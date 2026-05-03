package io.silomaceff.augerlink.comms

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    /**
     * Boot the Reticulum router and LXMF identity. Returns the local
     * destination hash on success.
     *
     * Safe to call from any coroutine context; runs the actual work on
     * Dispatchers.IO.
     */
    suspend fun init(context: Context): Result<String> = withContext(Dispatchers.IO) {
        ensureMulticastLockHeld(context.applicationContext)
        ensurePythonStarted(context.applicationContext)

        val py = Python.getInstance()
        val module = py.getModule(MODULE)
        val filesDir = context.filesDir.absolutePath

        Log.i(TAG, "Calling $MODULE.init(filesDir=$filesDir)")

        val result = module.callAttr("init", filesDir)
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
     * Acquire a Wi-Fi MulticastLock so AutoInterface's link-local IPv6
     * peer-discovery multicast actually leaves the device. Without this,
     * Android's userspace-multicast filter drops every send with EPERM
     * and AutoInterface logs "carrier loss" warnings on every announce.
     *
     * The lock is held for the lifetime of the process — Phase 5 service
     * migration will move acquire/release into the foreground service so
     * background reception keeps working with predictable lock lifecycle.
     */
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
