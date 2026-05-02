package io.silomaceff.augerlink.ui.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.silomaceff.augerlink.data.Contact
import io.silomaceff.augerlink.data.MockStore
import io.silomaceff.augerlink.ui.chats.ContactAvatar
import io.silomaceff.augerlink.ui.theme.AugerLinkMonospaceSmall

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    onOpenContact: (contactId: String) -> Unit,
    onAddContact: () -> Unit,
) {
    val contacts = remember { MockStore.contacts.sortedBy { it.displayName.lowercase() } }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContact,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "Add contact")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(padding)) {
            item { io.silomaceff.augerlink.ui.chats.ScreenTitle("Contacts") }
            items(contacts) { c ->
                ContactRow(c, onOpenContact)
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 80.dp),
                )
            }
        }
    }
}

@Composable
private fun ContactRow(c: Contact, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(c.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactAvatar(c, size = 48.dp)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = c.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                )
                if (c.isVerified) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = "verified",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Text(
                text = c.role,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${c.destinationHash.take(8)}…${c.destinationHash.takeLast(8)}",
                style = AugerLinkMonospaceSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
