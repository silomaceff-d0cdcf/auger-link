package io.silomaceff.augerlink.ui.voice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.silomaceff.augerlink.audio.VoiceCaptureController
import io.silomaceff.augerlink.audio.VoiceCaptureState

/**
 * Visible feedback strip shown above the chat composer while voice
 * capture is active. Renders a state label and the live partial
 * transcript so the user can see what Vosk is hearing in real time.
 *
 * Hidden when state is Idle. Shows on Initializing/Recording, and
 * lingers briefly with an error message on Error.
 */
@Composable
fun VoiceRecordingBanner(
    controller: VoiceCaptureController,
    modifier: Modifier = Modifier,
) {
    val state by controller.state.collectAsState()
    var partial by remember { mutableStateOf("") }

    LaunchedEffect(controller) {
        controller.partials.collect { partial = it }
    }
    LaunchedEffect(state) {
        // Reset the partial preview when capture stops/errors so a stale
        // transcript doesn't linger into the next press.
        if (state == VoiceCaptureState.Idle) partial = ""
    }

    val visible = state != VoiceCaptureState.Idle

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                val label = when (state) {
                    VoiceCaptureState.Initializing -> "Loading speech model…"
                    VoiceCaptureState.Recording ->
                        if (partial.isEmpty()) "Listening…" else "Listening · $partial"
                    VoiceCaptureState.Error -> "Voice recording failed"
                    VoiceCaptureState.Idle -> ""
                }
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                )
            }
        }
    }
}
