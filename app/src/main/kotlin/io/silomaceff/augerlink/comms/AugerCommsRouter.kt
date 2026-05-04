package io.silomaceff.augerlink.comms

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Snapshot of an LXMF message delivered by RNS, drained from the Python
 * `_inbox` deque via [AugerCommsRouter.pollIncoming].
 */
data class IncomingMessage(
    val sourceHashHex: String,
    val title: String,
    val content: String,
    /** Sender-claimed timestamp, seconds since epoch. */
    val timestamp: Double,
    /** Whether RNS verified the message signature against the sender's identity. */
    val signatureValid: Boolean,
)

/**
 * Bridge from Kotlin to the Python `auger_comms` module that wraps Reticulum
 * + LXMF.
 *
 * Phase 2 step 3 — minimal `init` that boots the Reticulum router + LXMF
 * identity + LXMRouter and returns the local LXMF destination hash. Send
 * and receive surfaces arrive in subsequent microcommits.
 *
 * Threading: every call enters Python on Dispatchers.IO. RNS internals are
 * thread-safe but blocking, and we never want them on the main thread.
 *
 * Idempotence: the Python side stashes singletons at module level; calling
 * [init] more than once returns the same destination hash (with a `note` of
 * "already initialized").
 */
object AugerCommsRouter {

    private const val TAG = "AugerCommsRouter"
    private const val MODULE = "auger_comms"

    @Volatile
    private var pythonStarted: Boolean = false

    @Volatile
    private var multicastLock: WifiManager.MulticastLock? = null

    private val _incomingMessages = MutableSharedFlow<IncomingMessage>(
        replay = 0,
        extraBufferCapacity = 64,
    )

    /** Emits each LXMF message delivered by the Python side. */
    val incomingMessages: SharedFlow<IncomingMessage> = _incomingMessages.asSharedFlow()

    @Volatile
    private var receiverJob: Job? = null

    /**
     * Start the receiver poll loop on [scope]. Polls Python's `_inbox`
     * every [pollIntervalMs] and emits each drained message to
     * [incomingMessages]. Idempotent — second call returns the existing
     * job. Cancel the job (or the scope) to stop polling.
     */
    @Synchronized
    fun startReceiverLoop(
        scope: CoroutineScope,
        pollIntervalMs: Long = 1500L,
    ): Job {
        receiverJob?.let { return it }
        val job = scope.launch(Dispatchers.IO) {
            Log.i(TAG, "receiver loop started (interval=${pollIntervalMs}ms)")
            while (isActive) {
                if (!Python.isStarted()) {
                    // Service onCreate runs before MainActivity has a chance to
                    // call init(). Skip until Python is up rather than logging
                    // a misleading "poll failed" warning.
                    delay(pollIntervalMs)
                    continue
                }
                try {
                    val batch = pollIncoming()
                    if (batch.isNotEmpty()) {
                        Log.i(TAG, "receiver loop drained ${batch.size} message(s)")
                        for (msg in batch) {
                            _incomingMessages.tryEmit(msg)
                        }
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "receiver loop poll failed: ${t.message}")
                }
                delay(pollIntervalMs)
            }
            Log.i(TAG, "receiver loop stopped")
        }
        receiverJob = job
        return job
    }

    /**
     * Boot the Reticulum router and LXMF identity. Returns the local
     * destination hash on success.
     *
     * Safe to call from any coroutine context; runs the actual work on
     * Dispatchers.IO.
     */
    /**
     * Boot the Reticulum router and LXMF identity. Returns the local
     * destination hash on success.
     *
     * @param tcpTargetsCsv optional comma-separated list of "host:port"
     *   entries, each becoming a TCPClientInterface alongside AutoInterface.
     *   Empty (default) for AutoInterface-only — fine on platforms where
     *   userspace IPv6 multicast works, but currently EPERM'd on Android
     *   even with MulticastLock held. Settings UI for entering these
     *   targets lands in a later microcommit; for now the value is
     *   threaded through so future callers don't need a signature change.
     */
    suspend fun init(
        context: Context,
        tcpTargetsCsv: String = "",
    ): Result<String> = withContext(Dispatchers.IO) {
        ensureMulticastLockHeld(context.applicationContext)
        ensurePythonStarted(context.applicationContext)

        val py = Python.getInstance()
        val module = py.getModule(MODULE)
        val filesDir = context.filesDir.absolutePath

        Log.i(TAG, "Calling $MODULE.init(filesDir=$filesDir, tcp_targets='$tcpTargetsCsv')")

        val result = module.callAttr("init", filesDir, tcpTargetsCsv)
        val ok = result.callAttr("get", "ok").toBoolean()
        if (ok) {
            val dest = result.callAttr("get", "lxmf_dest").toString()
            val note = result.callAttr("get", "note")?.toString()
            if (note != null) {
                Log.i(TAG, "Reticulum router already up — lxmf_dest=$dest")
            } else {
                Log.i(TAG, "Reticulum router up — lxmf_dest=$dest")
            }
            Result.success(dest)
        } else {
            val error = result.callAttr("get", "error").toString()
            Log.e(TAG, "init failed: $error")
            Result.failure(RuntimeException(error))
        }
    }

    /**
     * Send a text LXMF message. Returns the hex hash of the queued
     * outbound message on success.
     *
     * Success means LXMRouter accepted the message for delivery, NOT
     * that the recipient has received it. Delivery is async and surfaces
     * via the receive callback (next microcommit).
     *
     * If RNS doesn't yet know a path to the destination, this fails with
     * a "path not known yet" error and the Python side fires a path
     * request in the background. Caller should retry after a few seconds.
     */
    suspend fun send(
        destinationHashHex: String,
        content: String,
        title: String = "",
    ): Result<String> = withContext(Dispatchers.IO) {
        val py = Python.getInstance()
        val module = py.getModule(MODULE)

        val result = module.callAttr("send_message", destinationHashHex, content, title)
        val ok = result.callAttr("get", "ok").toBoolean()
        if (ok) {
            val lxmHash = result.callAttr("get", "lxm_hash")?.toString().orEmpty()
            Log.i(TAG, "send queued (len=${content.length}, lxm_hash_len=${lxmHash.length})")
            Result.success(lxmHash)
        } else {
            val error = result.callAttr("get", "error").toString()
            Log.w(TAG, "send failed: $error")
            Result.failure(RuntimeException(error))
        }
    }

    /**
     * Drain inbound messages delivered since the last poll. Returns an
     * empty list if no new messages.
     *
     * The Python side's `_inbox` deque is bounded at 200 entries; the
     * caller is expected to poll often enough that overflow doesn't
     * happen (a 1-2 second cadence is plenty for human-paced traffic).
     */
    suspend fun pollIncoming(): List<IncomingMessage> = withContext(Dispatchers.IO) {
        val py = Python.getInstance()
        val module = py.getModule(MODULE)
        val raw = module.callAttr("get_incoming")
        val size = raw.callAttr("__len__").toInt()
        if (size == 0) return@withContext emptyList()
        val out = ArrayList<IncomingMessage>(size)
        for (i in 0 until size) {
            val item = raw.callAttr("__getitem__", i)
            out.add(
                IncomingMessage(
                    sourceHashHex = item.callAttr("get", "source_hash")?.toString().orEmpty(),
                    title = item.callAttr("get", "title")?.toString().orEmpty(),
                    content = item.callAttr("get", "content")?.toString().orEmpty(),
                    timestamp = item.callAttr("get", "timestamp")?.toDouble() ?: 0.0,
                    signatureValid = item.callAttr("get", "signature_valid")?.toBoolean() == true,
                )
            )
        }
        out
    }

    /**
     * Acquire a Wi-Fi MulticastLock so AutoInterface's link-local IPv6
     * peer-discovery multicast actually leaves the device. Without this,
     * Android's userspace-multicast filter drops every send with EPERM
     * and AutoInterface logs "carrier loss" warnings on every announce.
     *
     * The lock is held for the lifetime of the process — Phase 5 service
     * migration will move acquire/release into the foreground service so
     * background reception keeps working with predictable lock lifecycle.
     */
    /**
     * Release the WiFi MulticastLock if held. Called by [AugerLinkService.onDestroy]
     * for clean shutdown — the process usually dies along with the service so
     * the kernel reclaims the lock anyway, but cleaning up explicitly avoids
     * a stale-lock complaint if Android ever stops the FGS without killing
     * the process.
     */
    @Synchronized
    fun releaseMulticastLockIfHeld() {
        multicastLock?.let { lock ->
            if (lock.isHeld) {
                lock.release()
                Log.i(TAG, "MulticastLock released")
            }
        }
        multicastLock = null
    }

    @Synchronized
    private fun ensureMulticastLockHeld(applicationContext: Context) {
        if (multicastLock?.isHeld == true) return
        val wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = wifi.createMulticastLock("AugerLink.Comms").apply {
            setReferenceCounted(false)
            acquire()
        }
        multicastLock = lock
        Log.i(TAG, "MulticastLock acquired (held=${lock.isHeld})")
    }

    @Synchronized
    private fun ensurePythonStarted(applicationContext: Context) {
        if (pythonStarted) return
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(applicationContext))
            Log.i(TAG, "Chaquopy Python runtime started")
        }
        pythonStarted = true
    }
}
