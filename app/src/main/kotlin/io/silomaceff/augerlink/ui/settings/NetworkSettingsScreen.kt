package io.silomaceff.augerlink.ui.settings

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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.launch

/**
 * Phase 4 step 1: edit the TCP peer target list ("host:port,host2:port2,...").
 *
 * The value is read at app boot by [io.silomaceff.augerlink.MainActivity]
 * and passed into [io.silomaceff.augerlink.comms.AugerCommsRouter.init] as
 * the `tcpTargetsCsv` argument, which becomes one or more
 * [[TCPClientInterface]] blocks in the live Reticulum config.
 *
 * Empty value means "no TCP peers" — AutoInterface multicast still works
 * on its own. A change takes effect on the NEXT app launch (Phase 5 will
 * make the router restartable in-place).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    var current by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        current = AugerLinkPrefs.readTcpTargets(context)
        loaded = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network") },
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
                text = "Pi LXMF target",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Comma-separated host:port peers (e.g. raspberrypi.local:4242). " +
                    "Leave empty to rely on local multicast peer discovery only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = current,
                onValueChange = { current = it },
                singleLine = true,
                enabled = loaded,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("host:port") },
            )
            Button(
                onClick = {
                    scope.launch {
                        AugerLinkPrefs.writeTcpTargets(context, current.trim())
                        snackbarHost.showSnackbar(
                            "Saved. Restart AugerLink to apply.",
                        )
                    }
                },
                enabled = loaded,
            ) {
                Text("Save")
            }
        }
    }
}
