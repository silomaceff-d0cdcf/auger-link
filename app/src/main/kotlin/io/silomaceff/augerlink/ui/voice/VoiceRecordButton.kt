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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.silomaceff.augerlink.audio.VoiceCaptureController
import io.silomaceff.augerlink.audio.VoiceCaptureState
import kotlinx.coroutines.launch

/**
 * Mic button for the chat composer. Long-press to record a voice
 * utterance; release to stop, transcribe, and pipe the result into the
 * caller's draft text via [onPartialTranscript] / [onFinalTranscript].
 *
 * Phase 6a-4 scope: this only does record-then-transcribe-into-text-field.
 * Phase 6b will add opus encode + voice attachment send for messages that
 * the user wants to send AS audio rather than as text.
 *
 * Permission flow: tap-and-hold without RECORD_AUDIO triggers the runtime
 * permission request once; the recording does not begin until the
 * permission is granted on the next press.
 */
@Composable
fun VoiceRecordButton(
    onPartialTranscript: (String) -> Unit,
    onFinalTranscript: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = remember { VoiceCaptureController(context) }
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

    LaunchedEffect(Unit) {
        controller.partials.collect(onPartialTranscript)
    }
    LaunchedEffect(Unit) {
        controller.finals.collect(onFinalTranscript)
    }
    DisposableEffect(Unit) {
        onDispose { controller.release() }
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
