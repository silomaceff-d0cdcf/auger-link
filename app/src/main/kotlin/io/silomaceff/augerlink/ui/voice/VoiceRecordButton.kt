package io.silomaceff.augerlink.ui.voice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.silomaceff.augerlink.audio.VoiceCaptureController
import io.silomaceff.augerlink.audio.VoiceCaptureState
import kotlinx.coroutines.launch

/**
 * Mic button that drives a [VoiceCaptureController] via long-press gesture.
 *
 * The controller is hoisted to the caller so a sibling composable
 * ([VoiceRecordingBanner]) can read its state and render a visible
 * "Listening…" banner during the press. Caller is responsible for
 * controller lifecycle ([VoiceCaptureController.release] on dispose).
 *
 * Long-press flow:
 *   1. press down → if no RECORD_AUDIO permission, request it; gesture ends.
 *   2. press down with permission → controller.start(); state turns Recording.
 *   3. release → controller.stop(); transcript final flushed.
 *
 * The first long-press after a fresh install requests RECORD_AUDIO and
 * does NOT start recording on that gesture (Android's permission dialog
 * cancels the touch). Subsequent presses begin recording immediately.
 */
@Composable
fun VoiceRecordButton(
    controller: VoiceCaptureController,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val state by controller.state.collectAsState()

    var pendingPermissionStart by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && pendingPermissionStart) {
            scope.launch { controller.start() }
        }
        pendingPermissionStart = false
    }

    val tint = when (state) {
        VoiceCaptureState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
        VoiceCaptureState.Initializing -> MaterialTheme.colorScheme.tertiary
        VoiceCaptureState.Recording -> MaterialTheme.colorScheme.error
        VoiceCaptureState.Error -> MaterialTheme.colorScheme.error
    }
    val description = when (state) {
        VoiceCaptureState.Idle -> "Hold to record voice message"
        VoiceCaptureState.Initializing -> "Loading speech model…"
        VoiceCaptureState.Recording -> "Recording (release to stop)"
        VoiceCaptureState.Error -> "Voice recording unavailable"
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (!controller.hasPermission()) {
                            pendingPermissionStart = true
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@detectTapGestures
                        }
                        scope.launch { controller.start() }
                        try {
                            tryAwaitRelease()
                        } finally {
                            scope.launch { controller.stop() }
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(28.dp),
        )
    }
}
