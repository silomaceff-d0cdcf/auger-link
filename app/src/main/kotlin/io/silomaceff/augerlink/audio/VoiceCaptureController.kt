package io.silomaceff.augerlink.audio

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * High-level state holder that wires [AudioCapture] into [VoskRecognizer]
 * for the long-press voice-record gesture.
 *
 * Single instance per chat composer. Lifecycle is bound to its host
 * Composable; the caller invokes [release] when the Composable leaves
 * composition.
 */
class VoiceCaptureController(context: Context) {

    private val capture = AudioCapture(context)
    private val recognizer = VoskRecognizer(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(VoiceCaptureState.Idle)
    val state: StateFlow<VoiceCaptureState> = _state.asStateFlow()

    val partials: Flow<String> = recognizer.partialTranscripts
    val finals: Flow<String> = recognizer.finalTranscripts

    private var captureJob: Job? = null

    fun hasPermission(): Boolean = capture.hasPermission()

    suspend fun start() {
        if (_state.value == VoiceCaptureState.Recording) return
        if (!capture.hasPermission()) {
            Log.w(TAG, "start: RECORD_AUDIO not granted")
            return
        }
        Log.i(TAG, "start: initializing")
        _state.value = VoiceCaptureState.Initializing
        runCatching { recognizer.init() }.onFailure {
            Log.e(TAG, "Vosk init failed", it)
            _state.value = VoiceCaptureState.Error
            return
        }
        runCatching { capture.start() }.onFailure {
            Log.e(TAG, "AudioCapture start failed", it)
            _state.value = VoiceCaptureState.Error
            return
        }
        _state.value = VoiceCaptureState.Recording
        Log.i(TAG, "start: recording")
        captureJob = scope.launch {
            var frameCount = 0
            capture.frames.collect { frame ->
                recognizer.feed(frame)
                frameCount++
                if (frameCount % 33 == 0) {
                    Log.d(TAG, "fed $frameCount frames (~${frameCount * 30}ms audio)")
                }
            }
            Log.i(TAG, "frame collector exited after $frameCount frames")
        }
    }

    suspend fun stop() {
        Log.i(TAG, "stop: state=${_state.value}")
        if (_state.value != VoiceCaptureState.Recording) {
            _state.value = VoiceCaptureState.Idle
            return
        }
        capture.stop()
        // Wait for the frame-feed coroutine to finish before invoking
        // recognizer.flushFinal — Vosk's native decoder is not safe to
        // call concurrently with acceptWaveForm, and a previous version
        // crashed in libvosk.so's lattice decoder when stop() raced the
        // last in-flight feed() call.
        captureJob?.cancelAndJoin()
        captureJob = null
        recognizer.flushFinal()
        _state.value = VoiceCaptureState.Idle
        Log.i(TAG, "stop: done, back to Idle")
    }

    fun release() {
        capture.release()
        recognizer.release()
        scope.cancel()
    }

    companion object {
        private const val TAG = "VoiceCaptureController"
    }
}

enum class VoiceCaptureState {
    Idle,
    Initializing,
    Recording,
    Error,
}
