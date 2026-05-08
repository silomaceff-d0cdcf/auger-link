package io.silomaceff.augerlink.ui.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import io.silomaceff.augerlink.audio.AmbientWakeController
import kotlinx.coroutines.launch

/**
 * Mounts an always-on "Hey Silo" wake-phrase listener for the duration
 * of the host chat screen. While active, the device mic stays open and
 * Vosk transcribes continuously; when the wake phrase is detected, the
 * caller's [onWake] handler fires, after which the next utterance final
 * arrives via [onCommandTranscript].
 *
 * Single-listener-per-screen by design. Users on multiple stacked
 * chat screens (deep navigation) would otherwise contend for the mic;
 * since AndroidRecord does not support shared captures, the listener
 * lives at the open-chat scope.
 *
 * Disabled by default ([enabled]=false) — privacy and battery cost both
 * argue for explicit opt-in. Phase 6 settings work will surface a
 * persistent toggle; for now the host wires a constant or in-memory
 * preference.
 */
@Composable
fun AmbientWakeListener(
    enabled: Boolean,
    onWake: () -> Unit,
    onCommandTranscript: (String) -> Unit,
) {
    if (!enabled) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = remember { AmbientWakeController(context) }

    LaunchedEffect(Unit) {
        scope.launch { controller.start() }
        controller.wakeEvents.collect { onWake() }
    }
    LaunchedEffect(Unit) {
        controller.commandTranscripts.collect(onCommandTranscript)
    }
    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }
}
