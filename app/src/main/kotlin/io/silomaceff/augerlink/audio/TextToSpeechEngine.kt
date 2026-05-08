package io.silomaceff.augerlink.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Phase 6c: lazy singleton wrapper around Android's [TextToSpeech].
 *
 * Initialization is async (the OS spins up a TTS service the first time)
 * so callers should treat the first [speak] after construction as a no-op
 * if [isReady] is false; subsequent calls work normally.
 *
 * Locale is en-US by default; Phase 6c future work will surface a setting
 * for users who want a different voice.
 */
object TextToSpeechEngine {

    private const val TAG = "TextToSpeechEngine"

    @Volatile
    private var tts: TextToSpeech? = null
    private val ready = AtomicBoolean(false)

    val isReady: Boolean
        get() = ready.get()

    /**
     * Idempotent. Safe to call multiple times; the first invocation kicks
     * off the TTS service initialization, subsequent calls return immediately.
     */
    fun init(context: Context) {
        if (tts != null) return
        synchronized(this) {
            if (tts != null) return
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        Log.w(TAG, "TTS en-US not available; downstream speak() will no-op")
                        ready.set(false)
                    } else {
                        ready.set(true)
                    }
                } else {
                    Log.w(TAG, "TextToSpeech init failed (status=$status)")
                    ready.set(false)
                }
            }
        }
    }

    /**
     * Speak [text] via the cached engine. No-op if the engine is not yet
     * ready or if the text is blank. Each call replaces any in-flight
     * utterance — the chat is fast, the speech doesn't queue.
     */
    fun speak(text: String) {
        if (!ready.get() || text.isBlank()) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready.set(false)
    }
}
