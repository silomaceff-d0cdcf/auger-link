package io.silomaceff.augerlink.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Phase 6a wake-phrase detector. Watches a stream of partial speech
 * transcripts (from a continuously-running Vosk recognizer) and emits
 * an event when "hey silo" — or close variants — appears.
 *
 * Matching is intentionally loose: real-world ASR commonly drops the
 * leading "hey" function word, mishears the silibant initial of "silo",
 * or returns plurals. The regex below accepts the wake phrase whether
 * the user says any of: "hey silo", "silo", "hi silo", "ay silo".
 *
 * False-positive surface: any utterance containing the word "silo" will
 * trigger. Acceptable for a farm-context app, where "silo" rarely shows
 * up in casual conversation. If false positives become a problem, tighten
 * the regex to require the prefix word.
 *
 * Cooldown: after a trigger, the detector ignores subsequent matches for
 * [cooldownMillis] so the same utterance does not fire twice as the
 * partial transcript stabilizes.
 */
class WakeWordDetector(
    private val cooldownMillis: Long = DEFAULT_COOLDOWN_MILLIS,
) {

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val events: Flow<Unit> = _events.asSharedFlow()

    private var lastTriggerAt: Long = 0L

    /**
     * Push a partial or final transcript at the detector. Triggers an
     * event on [events] if the wake phrase is found and the cooldown has
     * elapsed since the last trigger.
     *
     * Returns true if a trigger fired this call.
     */
    suspend fun observe(transcript: String): Boolean {
        if (!WAKE_PATTERN.containsMatchIn(transcript)) return false
        val now = System.currentTimeMillis()
        if (now - lastTriggerAt < cooldownMillis) return false
        lastTriggerAt = now
        _events.emit(Unit)
        return true
    }

    /** Reset cooldown so the next match fires immediately (testing aid). */
    fun reset() {
        lastTriggerAt = 0L
    }

    companion object {
        const val DEFAULT_COOLDOWN_MILLIS: Long = 2_500L

        /**
         * Matches any of: "silo", "silos", "siloh", with an optional
         * single-word prefix like "hey", "hi", "ay", or "a".
         * Case-insensitive; word-boundary anchored.
         */
        private val WAKE_PATTERN = Regex(
            pattern = "\\b(?:hey|hi|ay|a)?\\s*silo[hs]?\\b",
            options = setOf(RegexOption.IGNORE_CASE),
        )
    }
}
