package io.silomaceff.augerlink.ui.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.silomaceff.augerlink.comms.AugerCommsRouter
import io.silomaceff.augerlink.data.AugerLinkDatabase
import io.silomaceff.augerlink.data.AugerLinkPrefs
import io.silomaceff.augerlink.data.MessageDirection
import io.silomaceff.augerlink.data.MessageStatus
import io.silomaceff.augerlink.data.PersistedMessage
import io.silomaceff.augerlink.audio.TextToSpeechEngine
import io.silomaceff.augerlink.ui.theme.AugerLinkMonospaceSmall
import io.silomaceff.augerlink.ui.util.TimeFormat
import io.silomaceff.augerlink.ui.voice.AmbientWakeListener
import io.silomaceff.augerlink.ui.voice.VoiceRecordButton
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Phase 4: dedicated chat UI for a single user-added contact, backed by
 * the Room `messages` table.
 *
 * Replaces the c_9 step 4d session-only `mutableStateListOf` — message
 * history now persists across app restarts. Subscribes to the live DAO
 * Flow filtered by destination hash; outbound sends insert a Sending row
 * and update it to Delivered or Failed based on router result; inbound
 * deliveries insert with the existing Delivered status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserContactChatScreen(
    destinationHashHex: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AugerLinkDatabase.get(context) }
    val dao = remember { db.messageDao() }

    val targetHash = destinationHashHex.lowercase()
    val userContacts by AugerLinkPrefs.contactsFlow(context).collectAsState(initial = emptyList())
    val contact = userContacts.firstOrNull {
        it.destinationHashHex.equals(targetHash, ignoreCase = true)
    }
    val displayName = contact?.name ?: "Unknown peer"

    val messages by dao.observeForContact(targetHash).collectAsState(initial = emptyList())
    var draft by remember { mutableStateOf("") }

    // Phase 6a-5: ambient "Hey Silo" listener. Default OFF — privacy +
    // battery argue for explicit opt-in via a Settings toggle (lands as
    // follow-up). Mic-contention with the long-press path will need an
    // arbitration layer if both are ever simultaneously enabled.
    val ambientWakeEnabled = false
    AmbientWakeListener(
        enabled = ambientWakeEnabled,
        onWake = { /* visual cue could land here once UX is decided */ },
        onCommandTranscript = { transcript -> draft = transcript },
    )

    // Phase 6c: prime the TTS engine when this screen is first composed so
    // the first auto-speak doesn't pay the engine-init latency mid-message.
    LaunchedEffect(Unit) { TextToSpeechEngine.init(context) }

    val autoSpeak by AugerLinkPrefs
        .autoSpeakInboundFlow(context)
        .collectAsState(initial = false)

    // Subscribe to incoming LXMF deliveries for this contact and persist them.
    LaunchedEffect(targetHash) {
        AugerCommsRouter.incomingMessages
            .filter { it.sourceHashHex.lowercase() == targetHash }
            .collect { incoming ->
                val sentAtMs = if (incoming.timestamp > 0)
                    (incoming.timestamp * 1000).toLong()
                else System.currentTimeMillis()
                dao.insert(
                    PersistedMessage(
                        id = "in-$sentAtMs-${incoming.content.hashCode()}",
                        contactDestHash = targetHash,
                        direction = MessageDirection.Inbound.name,
                        body = incoming.content,
                        sentAt = sentAtMs,
                        status = MessageStatus.Delivered.name,
                    )
                )
                // Read aloud after persistence — keeps history correct even
                // if the speak() throws or the engine isn't ready.
                if (autoSpeak) TextToSpeechEngine.speak(incoming.content)
            }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${targetHash.take(8)}…",
                    style = AugerLinkMonospaceSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        bottomBar = {
            Composer(
                draft = draft,
                onDraftChange = { draft = it },
                onSend = {
                    val body = draft.trim()
                    if (body.isNotEmpty()) {
                        val nowMs = System.currentTimeMillis()
                        val pending = PersistedMessage(
                            id = "out-$nowMs",
                            contactDestHash = targetHash,
                            direction = MessageDirection.Outbound.name,
                            body = body,
                            sentAt = nowMs,
                            status = MessageStatus.Sending.name,
                        )
                        draft = ""
                        scope.launch {
                            dao.insert(pending)
                            val result = AugerCommsRouter.send(
                                destinationHashHex = targetHash,
                                content = body,
                            )
                            dao.update(
                                pending.copy(
                                    status = if (result.isSuccess) MessageStatus.Delivered.name
                                             else MessageStatus.Failed.name,
                                )
                            )
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No messages yet. Send one to test the link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages, key = { it.id }) { entry -> Bubble(entry) }
            }
        }
    }
}

@Composable
private fun Bubble(entry: PersistedMessage) {
    val isOut = entry.direction == MessageDirection.Outbound.name
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOut) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (isOut) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isOut) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(
                topStart = if (isOut) 16.dp else 4.dp,
                topEnd = if (isOut) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp,
            ),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(0.85f),
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = entry.body,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(2.dp))
                val statusLabel = when (entry.status) {
                    MessageStatus.Sending.name -> "sending…"
                    MessageStatus.Delivered.name -> if (isOut) "queued" else "received"
                    MessageStatus.Failed.name -> "failed"
                    else -> entry.status.lowercase()
                }
                Text(
                    text = "${TimeFormat.timeOnly(Instant.ofEpochMilli(entry.sentAt))} · $statusLabel",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = if (isOut)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Composer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    placeholder = { Text("Message…") },
                    modifier = Modifier.weight(1f),
                    minLines = 1,
                    maxLines = 6,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
                Spacer(Modifier.width(4.dp))
                VoiceRecordButton(
                    onPartialTranscript = onDraftChange,
                    onFinalTranscript = onDraftChange,
                )
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = onSend,
                    enabled = draft.isNotBlank(),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
