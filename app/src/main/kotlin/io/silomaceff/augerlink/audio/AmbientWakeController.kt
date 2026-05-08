package io.silomaceff.augerlink.audio

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Always-on listener that pairs [AudioCapture] + [VoskRecognizer] +
 * [WakeWordDetector] to provide a hands-free "Hey Silo" trigger.
 *
 * Two operating modes, internal:
 * - **Listening**: mic open, recognizer running, partials feed the wake
 *   detector; finals are discarded.
 * - **Capturing**: wake fired; the next final transcript is treated as
 *   the user's command, emitted via [commandTranscripts], and the
 *   controller returns to Listening.
 *
 * Mic contention: [AmbientWakeController] holds the mic for the entire
 * lifetime between [start] and [release]. A separate
 * [VoiceCaptureController] (long-press path) cannot run concurrently;
 * the host UI is responsible for gating one or the other.
 */
class AmbientWakeController(context: Context) {

    private val capture = AudioCapture(context)
    private val recognizer = VoskRecognizer(context)
    private val detector = WakeWordDetector()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _wakeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    private val _commandTranscripts = MutableSharedFlow<String>(extraBufferCapacity = 4)

    val wakeEvents: Flow<Unit> = _wakeEvents.asSharedFlow()
    val commandTranscripts: Flow<String> = _commandTranscripts.asSharedFlow()

    @Volatile
    private var captureCommandNext: Boolean = false
    private var captureJob: Job? = null
    private var partialJob: Job? = null
    private var finalJob: Job? = null

    suspend fun start() {
        if (!capture.hasPermission()) {
            Log.w(TAG, "RECORD_AUDIO not granted; ambient listener inactive")
            return
        }
        runCatching { recognizer.init() }.onFailure {
            Log.e(TAG, "Vosk init failed for ambient listener", it)
            return
        }
        runCatching { capture.start() }.onFailure {
            Log.e(TAG, "AudioCapture start failed for ambient listener", it)
            return
        }

        captureJob = scope.launch {
            capture.frames.collect { frame -> recognizer.feed(frame) }
        }
        partialJob = scope.launch {
            recognizer.partialTranscripts.collect { partial ->
                if (detector.observe(partial)) {
                    captureCommandNext = true
                    _wakeEvents.emit(Unit)
                }
            }
        }
        finalJob = scope.launch {
            recognizer.finalTranscripts.collect { final ->
                if (captureCommandNext) {
                    captureCommandNext = false
                    _commandTranscripts.emit(stripWakePrefix(final))
                }
            }
        }
    }

    fun release() {
        capture.release()
        recognizer.release()
        scope.cancel()
        captureJob = null
        partialJob = null
        finalJob = null
    }

    /**
     * The wake utterance often arrives glued onto the front of the
     * command (Vosk emits the whole utterance as one final). Strip the
     * common "hey silo" / "silo" prefix so the composer sees just the
     * command body.
     */
    private fun stripWakePrefix(transcript: String): String {
        val match = WAKE_PREFIX.find(transcript) ?: return transcript.trim()
        return transcript.substring(match.range.last + 1).trim()
    }

    companion object {
        private const val TAG = "AmbientWakeController"

        /** Same shape as WakeWordDetector but anchored to the start of the string. */
        private val WAKE_PREFIX = Regex(
            pattern = "^(?:hey|hi|ay|a)?\\s*silo[hs]?\\s*[,:.]?",
            options = setOf(RegexOption.IGNORE_CASE),
        )
    }
}
