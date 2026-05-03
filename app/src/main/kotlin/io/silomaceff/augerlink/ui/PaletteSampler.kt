package io.silomaceff.augerlink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.silomaceff.augerlink.ui.theme.AugerLinkColors
import io.silomaceff.augerlink.ui.theme.AugerLinkMonospaceMedium
import io.silomaceff.augerlink.ui.theme.AugerLinkMonospaceSmall
import io.silomaceff.augerlink.ui.theme.AugerLinkTheme

/**
 * Palette Sampler — the Phase 1 skeleton's first screen.
 *
 * This screen IS the palette specification rendered. Side-load the APK + open
 * the app + you can eyeball every named color token, every button state,
 * every Material 3 component as it will look in the real app, on the real
 * device, in real ambient light.
 *
 * Acts as the visual regression test: when palette tokens change, this screen
 * shows the diff at a glance.
 */

private data class TokenSwatch(
    val name: String,
    val hex: String,
    val color: Color,
    val role: String,
)

private val PALETTE_TOKENS: List<TokenSwatch> = listOf(
    TokenSwatch("dusk-amber",   "#9C5519", AugerLinkColors.DuskAmber,    "primary — top bar, FAB, primary buttons"),
    TokenSwatch("glow-orange",  "#D97826", AugerLinkColors.GlowOrange,   "primaryContainer — outgoing bubbles, highlights"),
    TokenSwatch("gold",         "#FFC065", AugerLinkColors.Gold,         "secondary — active indicators, notification dots"),
    TokenSwatch("cream",        "#F5E6D3", AugerLinkColors.Cream,        "onBackground / onSurface — body text"),
    TokenSwatch("cream-muted",  "#C8B8A0", AugerLinkColors.CreamMuted,   "onSurfaceVariant — secondary text"),
    TokenSwatch("barn-dark",    "#1A1410", AugerLinkColors.BarnDark,     "background — app canvas"),
    TokenSwatch("charcoal",     "#0F0E0C", AugerLinkColors.Charcoal,     "surface — cards, sheets, incoming bubbles"),
    TokenSwatch("night-teal",   "#2A3040", AugerLinkColors.NightTeal,    "surfaceVariant — separators, tinted accents"),
    TokenSwatch("divider",      "#2E2A26", AugerLinkColors.Divider,      "outline — section dividers"),
    TokenSwatch("error",        "#E53E2C", AugerLinkColors.ErrorWarm,    "error — destructive actions"),
    TokenSwatch("success",      "#7FA756", AugerLinkColors.SuccessOlive, "success — connected, delivered, verified"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaletteSamplerScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AugerLink Palette") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Phase-2 stub */ },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                Icon(Icons.Filled.Mic, contentDescription = "Voice")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            item { SectionHeader("Tokens") }
            items(PALETTE_TOKENS) { TokenRow(it) }

            item { SectionHeader("Buttons") }
            item { ButtonShowcase() }

            item { SectionHeader("Message bubbles") }
            item { BubbleShowcase() }

            item { SectionHeader("Status indicators") }
            item { StatusShowcase() }

            item { SectionHeader("Hash display (monospace)") }
            item { HashShowcase() }

            item { Spacer(Modifier.height(24.dp)) }
            item { Footer() }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun TokenRow(t: TokenSwatch) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(t.color, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = t.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = t.hex,
                    style = AugerLinkMonospaceMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = t.role,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ButtonShowcase() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Send (filled / primary)")
        }
        FilledTonalButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Tonal (primary container)")
        }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Outlined (secondary action)")
        }
        TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Text only (low emphasis)")
        }
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            Text("Destructive (error)")
        }
    }
}

@Composable
private fun BubbleShowcase() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Incoming — surface bg + onSurface text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(0.85f),
            ) {
                Text(
                    "Incoming message — drying corn 14% moisture, ready for storage.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
        // Outgoing — primaryContainer bg + onPrimaryContainer text (BARN-DARK on glow-orange — verified)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(0.85f),
            ) {
                Text(
                    "Outgoing reply — copy that, moving augers from bin 3 to bin 5.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun StatusShowcase() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusRow(Icons.Filled.CheckCircle, AugerLinkColors.SuccessOlive, "Reticulum interface UP — 3 peers visible")
        StatusRow(Icons.Filled.WarningAmber, AugerLinkColors.ErrorWarm,    "Last delivery failed — retrying in 30s")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(AugerLinkColors.Gold, CircleShape),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Active — voice trigger listening",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun StatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun HashShowcase() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "97fb c0ac 4ef5 92ae",
            style = AugerLinkMonospaceMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "verified-boot key SHA256 (Pixel 6a / GrapheneOS)",
            style = AugerLinkMonospaceSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Footer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "AugerLink 0.1.0-phase1 — palette sampler",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Sovereign mesh-comms · Reticulum + LXMF · Apache 2.0",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1410, heightDp = 1400)
@Composable
private fun PaletteSamplerPreview() {
    AugerLinkTheme {
        PaletteSamplerScreen()
    }
}
