package io.silomaceff.augerlink.ui.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.silomaceff.augerlink.data.AugerLinkPrefs
import io.silomaceff.augerlink.data.UserContact
import kotlinx.coroutines.launch

private val HEX_HASH_REGEX = Regex("^[0-9a-fA-F]{32}$")

/**
 * Phase 4 step 4: paste-a-destination-hash contact creation.
 *
 * Two fields — display name + 32-char hex destination hash. On Save the
 * contact is persisted via [AugerLinkPrefs.addContact] and the user is
 * popped back to the contact list.
 *
 * Validation is intentionally minimal: a non-blank name and a
 * 32-hex-character destination hash. Anything fancier (verifying the hash
 * is announceable, scanning a QR, importing from Sideband) is deferred.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var destHash by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add contact") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "New peer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Paste a 32-character LXMF destination hash and give it a name. " +
                    "The contact appears in your list immediately and persists across launches.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Display name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = destHash,
                onValueChange = { destHash = it },
                singleLine = true,
                label = { Text("Destination hash (32 hex chars)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val cleanHash = destHash.trim().lowercase()
                    when {
                        cleanName.isEmpty() -> scope.launch {
                            snackbarHost.showSnackbar("Display name required")
                        }
                        !HEX_HASH_REGEX.matches(cleanHash) -> scope.launch {
                            snackbarHost.showSnackbar("Destination hash must be 32 hex characters")
                        }
                        else -> scope.launch {
                            AugerLinkPrefs.addContact(
                                context,
                                UserContact(name = cleanName, destinationHashHex = cleanHash),
                            )
                            snackbarHost.showSnackbar("Contact saved")
                            onSaved()
                        }
                    }
                },
            ) {
                Text("Save contact")
            }
        }
    }
}
