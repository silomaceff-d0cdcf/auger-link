package io.silomaceff.augerlink.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * 16 kHz mono 16-bit PCM mic capture for the Phase 6 voice pipeline.
 *
 * The 16 kHz / 16-bit / mono shape is the lowest common denominator
 * required by both Vosk (offline STT) and most wake-word engines, so
 * one capture stream feeds both downstream consumers without resampling.
 *
 * Frames are emitted as 30 ms chunks (480 samples) — small enough that a
 * wake-word recognizer never sees more than ~30 ms of latency, large
 * enough that the per-frame Kotlin overhead is negligible.
 *
 * The caller owns the runtime RECORD_AUDIO permission; [hasPermission] is
 * a convenience check. Construction without the permission throws on
 * [start]; the caller should request the permission first.
 */
class AudioCapture(
    private val context: Context,
    private val sampleRateHz: Int = SAMPLE_RATE_HZ,
    private val frameSamples: Int = FRAME_SAMPLES,
) {

    /**
     * Hot stream of PCM frames once [start] succeeds. Each ShortArray is
     * exactly [frameSamples] in length. Backpressure is handled by a
     * conflated channel so a slow consumer drops old frames rather than
     * stalling the recorder thread.
     */
    val frames: Flow<ShortArray>
        get() = framesChannel.receiveAsFlow()

    private val framesChannel = Channel<ShortArray>(capacity = Channel.CONFLATED)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var record: AudioRecord? = null

    @Volatile
    var isRunning: Boolean = false
        private set

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Start the recorder. Idempotent — repeated calls while running are no-ops.
     * Throws SecurityException if RECORD_AUDIO has not been granted.
     */
    @SuppressLint("MissingPermission")
    fun start() {
        if (isRunning) return
        if (!hasPermission()) {
            throw SecurityException("RECORD_AUDIO permission not granted")
        }

        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRateHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        // Buffer at least 4× the frame size so the kernel ring stays ahead
        // of the consumer even under brief scheduling stalls.
        val bufferSize = maxOf(minBuffer, frameSamples * 2 * 4)

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRateHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            error("AudioRecord failed to initialize (sampleRateHz=$sampleRateHz)")
        }
        record = recorder
        recorder.startRecording()
        isRunning = true

        scope.launch {
            val frame = ShortArray(frameSamples)
            while (isActive && isRunning) {
                val read = recorder.read(frame, 0, frame.size)
                if (read <= 0) continue
                // Copy: the consumer Flow may outlive the next read() into the same buffer.
                framesChannel.trySend(frame.copyOf(read))
            }
        }
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        record?.let {
            runCatching { it.stop() }
            it.release()
        }
        record = null
    }

    fun release() {
        stop()
        scope.cancel()
        framesChannel.close()
    }

    companion object {
        const val SAMPLE_RATE_HZ = 16_000
        const val FRAME_SAMPLES = 480 // 30 ms at 16 kHz
    }
}
