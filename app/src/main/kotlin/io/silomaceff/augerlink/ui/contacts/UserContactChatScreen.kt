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
import androidx.compose.runtime.mutableStateListOf
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
import io.silomaceff.augerlink.data.AugerLinkPrefs
import io.silomaceff.augerlink.ui.theme.AugerLinkMonospaceSmall
import io.silomaceff.augerlink.ui.util.TimeFormat
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.Instant

private enum class ChatMessageDirection { Outbound, Inbound }
private enum class ChatMessageStatus { Sending, Delivered, Failed, Received }
private data class ChatMessageEntry(
    val id: String,
    val direction: ChatMessageDirection,
    val body: String,
    val sentAt: Instant,
    val status: ChatMessageStatus,
)

/**
 * Phase 4 step 4d: dedicated chat UI for a single user-added contact.
 *
 * Distinct from the MockStore-based [io.silomaceff.augerlink.ui.chats.ChatScreen]
 * — this one drives directly off [AugerLinkPrefs] (for the contact name)
 * and [AugerCommsRouter] (for live send + the incoming-message SharedFlow).
 * No mock data; every message in this screen came from a real LXMF round
 * trip during this app session.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserContactChatScreen(
    destinationHashHex: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userContacts by AugerLinkPrefs.contactsFlow(context).collectAsState(initial = emptyList())
    val contact = userContacts.firstOrNull {
        it.destinationHashHex.equals(destinationHashHex, ignoreCase = true)
    }
    val displayName = contact?.name ?: "Unknown peer"

    val messages = remember(destinationHashHex) { mutableStateListOf<ChatMessageEntry>() }
    var draft by remember { mutableStateOf("") }

    val targetHash = destinationHashHex.lowercase()
    LaunchedEffect(targetHash) {
        AugerCommsRouter.incomingMessages
            .filter { it.sourceHashHex.lowercase() == targetHash }
            .collect { incoming ->
                messages.add(
                    ChatMessageEntry(
                        id = "in-${System.currentTimeMillis()}-${messages.size}",
                        direction = ChatMessageDirection.Inbound,
                        body = incoming.content,
                        sentAt = if (incoming.timestamp > 0)
                            Instant.ofEpochSecond(incoming.timestamp.toLong())
                        else Instant.now(),
                        status = ChatMessageStatus.Received,
                    ),
                )
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
                        val pending = ChatMessageEntry(
                            id = "out-${System.currentTimeMillis()}",
                            direction = ChatMessageDirection.Outbound,
                            body = body,
                            sentAt = Instant.now(),
                            status = ChatMessageStatus.Sending,
                        )
                        messages.add(pending)
                        draft = ""
                        scope.launch {
                            val result = AugerCommsRouter.send(
                                destinationHashHex = targetHash,
                                content = body,
                            )
                            val idx = messages.indexOfFirst { it.id == pending.id }
                            if (idx >= 0) {
                                messages[idx] = pending.copy(
                                    status = if (result.isSuccess) ChatMessageStatus.Delivered
                                             else ChatMessageStatus.Failed,
                                )
                            }
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
                items(messages) { entry -> Bubble(entry) }
            }
        }
    }
}

@Composable
private fun Bubble(entry: ChatMessageEntry) {
    val isOut = entry.direction == ChatMessageDirection.Outbound
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
                    ChatMessageStatus.Sending -> "sending…"
                    ChatMessageStatus.Delivered -> "queued"
                    ChatMessageStatus.Failed -> "failed"
                    ChatMessageStatus.Received -> "received"
                }
                Text(
                    text = "${TimeFormat.timeOnly(entry.sentAt)} · $statusLabel",
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
                Spacer(Modifier.width(8.dp))
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
