package io.silomaceff.augerlink.audio

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Phase 6a offline speech-to-text using Vosk (Kaldi).
 *
 * The model lives in assets/model-en-us — a ~68 MB unzipped vosk-model-small-en-us-0.15.
 * On first run, [StorageService] copies the model from assets into the
 * app's private storage; thereafter [Model] mmaps the on-disk files
 * directly. The on-disk copy survives app data clears in the same way
 * normal app private storage does, so subsequent boots skip the unpack.
 *
 * Two output streams:
 * - [partialTranscripts]: live updates while the user is still speaking,
 *   useful for showing a typing-style preview in the composer.
 * - [finalTranscripts]: emitted on each utterance boundary detected by
 *   Vosk's voice-activity heuristic.
 */
class VoskRecognizer(
    private val context: Context,
    private val sampleRateHz: Int = AudioCapture.SAMPLE_RATE_HZ,
) {

    private val _partials = MutableSharedFlow<String>(extraBufferCapacity = 16)
    private val _finals = MutableSharedFlow<String>(extraBufferCapacity = 8)

    val partialTranscripts: Flow<String> = _partials.asSharedFlow()
    val finalTranscripts: Flow<String> = _finals.asSharedFlow()

    private var model: Model? = null
    private var recognizer: Recognizer? = null

    /**
     * Loads the Vosk model from assets (via StorageService unpack on first
     * run, direct path on subsequent runs) and constructs a recognizer
     * tuned to [sampleRateHz]. Suspends until the model is ready.
     */
    suspend fun init() {
        if (model != null) return
        val loadedModel = withContext(Dispatchers.IO) { unpackModel() }
        model = loadedModel
        recognizer = Recognizer(loadedModel, sampleRateHz.toFloat())
    }

    /**
     * Feeds one PCM frame into the recognizer. Emits a partial transcript
     * if the recognizer's still building up an utterance, or a final
     * transcript when it detects the utterance has closed.
     *
     * Caller is expected to drive this from [AudioCapture.frames] —
     * something like:
     *
     * ```
     * audioCapture.frames.collect { frame -> recognizer.feed(frame) }
     * ```
     */
    suspend fun feed(frame: ShortArray) {
        val rec = recognizer ?: return
        val end = rec.acceptWaveForm(frame, frame.size)
        if (end) {
            val text = JSONObject(rec.result).optString("text").trim()
            if (text.isNotEmpty()) {
                Log.i(TAG, "FINAL: '$text'")
                _finals.emit(text)
            }
        } else {
            val partial = JSONObject(rec.partialResult).optString("partial").trim()
            if (partial.isNotEmpty()) {
                Log.d(TAG, "partial: '$partial'")
                _partials.emit(partial)
            }
        }
    }

    /** Force the current utterance to close and emit any pending text. */
    suspend fun flushFinal() {
        val rec = recognizer ?: return
        val text = JSONObject(rec.finalResult).optString("text").trim()
        Log.i(TAG, "flushFinal: '${text}'")
        if (text.isNotEmpty()) _finals.emit(text)
    }

    fun release() {
        recognizer?.close()
        recognizer = null
        model?.close()
        model = null
    }

    /**
     * StorageService.unpack copies the model from assets to app private
     * storage on first call and constructs a usable [Model] pointing at
     * the actual on-disk path. Subsequent calls skip the copy and return
     * a fresh Model handle to the cached files.
     *
     * Critically, the returned Model points at the path Vosk expects —
     * we previously constructed our own Model from filesDir which was
     * the wrong location (StorageService writes to externalFilesDir on
     * some Android versions and nests the source-asset dirname inside
     * the target dir).
     */
    private suspend fun unpackModel(): Model =
        suspendCancellableCoroutine { cont ->
            StorageService.unpack(
                context,
                ASSET_MODEL_DIR,
                INTERNAL_MODEL_DIR,
                { unpacked: Model -> cont.resume(unpacked) },
                { err: java.io.IOException ->
                    Log.e(TAG, "Vosk model unpack failed", err)
                    cont.resumeWithException(err)
                },
            )
        }

    companion object {
        private const val TAG = "VoskRecognizer"
        private const val ASSET_MODEL_DIR = "model-en-us"
        private const val INTERNAL_MODEL_DIR = "vosk-model-en-us"
    }
}
