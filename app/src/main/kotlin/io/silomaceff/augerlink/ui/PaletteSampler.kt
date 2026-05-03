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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.silomaceff.augerlink.util.SunCalc
import io.silomaceff.augerlink.ui.theme.SUNSET_LEAD_MINUTES
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import io.silomaceff.augerlink.ui.theme.AugerLinkColorsDay
import io.silomaceff.augerlink.ui.theme.AugerLinkColorsNight
import io.silomaceff.augerlink.ui.theme.AugerLinkMonospaceMedium
import io.silomaceff.augerlink.ui.theme.AugerLinkMonospaceSmall
import io.silomaceff.augerlink.ui.theme.AugerLinkPaletteTokens
import io.silomaceff.augerlink.ui.theme.AugerLinkTheme
import io.silomaceff.augerlink.ui.theme.LocalAugerLinkTokens
import io.silomaceff.augerlink.ui.theme.PaletteLocation
import io.silomaceff.augerlink.ui.theme.PaletteMode
import io.silomaceff.augerlink.ui.theme.PaletteVariant
import io.silomaceff.augerlink.ui.theme.describeAutoState

/**
 * Palette Sampler — the Phase 1 skeleton's first screen.
 *
 * Renders every brand token in real Material 3 components on real device
 * hardware. A SegmentedButton toggle at the top switches between the
 * Night-shift (harvest-warm) and Day-shift (cool-mute) palette variants
 * for live A/B comparison.
 *
 * Acts as the visual regression test: when palette tokens change, this
 * screen shows the diff at a glance.
 */

private data class TokenSwatch(
    val name: String,
    val role: String,
    val pick: (AugerLinkPaletteTokens) -> Color,
)

private val TOKENS: List<TokenSwatch> = listOf(
    TokenSwatch("primary",            "top bar, FAB, primary buttons")        { it.primary },
    TokenSwatch("primaryContainer",   "outgoing bubbles, highlights")          { it.primaryContainer },
    TokenSwatch("secondary",          "active indicators, notification dots")  { it.secondary },
    TokenSwatch("onBackground",       "body text")                             { it.onBackground },
    TokenSwatch("onSurfaceVariant",   "secondary text")                        { it.onSurfaceVariant },
    TokenSwatch("background",         "app canvas")                            { it.background },
    TokenSwatch("surface",            "cards, sheets, incoming bubbles")       { it.surface },
    TokenSwatch("surfaceVariant",     "separators, tinted accents")            { it.surfaceVariant },
    TokenSwatch("divider",            "outline — section dividers")            { it.divider },
    TokenSwatch("error",              "destructive actions")                   { it.error },
    TokenSwatch("success",            "connected, delivered, verified")        { it.success },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaletteSamplerScreen(
    mode: PaletteMode,
    resolvedVariant: PaletteVariant,
    location: PaletteLocation,
    onModeChange: (PaletteMode) -> Unit,
) {
    val tokens = LocalAugerLinkTokens.current
    val titleSuffix = when (mode) {
        PaletteMode.Auto -> "Auto (${resolvedVariant.displayName})"
        PaletteMode.Day, PaletteMode.Night -> mode.displayName
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AugerLink · $titleSuffix") },
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
            item { ModeToggle(mode, onModeChange) }
            item { AutoDashboard(mode, resolvedVariant, location) }

            item { SectionHeader("Tokens") }
            items(TOKENS) { TokenRow(it, tokens) }

            item { SectionHeader("Buttons") }
            item { ButtonShowcase() }

            item { SectionHeader("Message bubbles") }
            item { BubbleShowcase() }

            item { SectionHeader("Status indicators") }
            item { StatusShowcase(tokens) }

            item { SectionHeader("Hash display (monospace)") }
            item { HashShowcase() }

            item { Spacer(Modifier.height(24.dp)) }
            item { Footer(mode, resolvedVariant) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeToggle(
    mode: PaletteMode,
    onModeChange: (PaletteMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        SingleChoiceSegmentedButtonRow {
            PaletteMode.entries.forEachIndexed { index, m ->
                SegmentedButton(
                    selected = (m == mode),
                    onClick = { onModeChange(m) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = PaletteMode.entries.size,
                    ),
                ) {
                    Text(m.displayName)
                }
            }
        }
    }
}

/**
 * Dashboard panel: date · clock · lat/lon · sunrise · sunset · flip-at · resolved variant.
 *
 * Visible in all modes (Auto / Day / Night) for transparency — shows WHY the
 * current variant is what it is. Clock + computed flip time tick once per
 * minute via produceState.
 */
@Composable
private fun AutoDashboard(
    mode: PaletteMode,
    resolvedVariant: PaletteVariant,
    location: PaletteLocation,
) {
    val now by produceState(
        initialValue = ZonedDateTime.now(location.zoneId),
    ) {
        while (isActive) {
            value = ZonedDateTime.now(location.zoneId)
            delay(60_000L)
        }
    }
    val today = now.toLocalDate()
    val tomorrow = today.plusDays(1)
    val sunToday: SunCalc.SunTimes? = remember(today, location) {
        SunCalc.sunriseSunset(today, location.latitudeDeg, location.longitudeDeg, location.zoneId)
    }
    val sunTomorrow: SunCalc.SunTimes? = remember(tomorrow, location) {
        SunCalc.sunriseSunset(tomorrow, location.latitudeDeg, location.longitudeDeg, location.zoneId)
    }

    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    val dateFmt = DateTimeFormatter.ofPattern("EEE  MMM d  yyyy")
    val nowTime = now.toLocalTime()
    val sunsetLead = SUNSET_LEAD_MINUTES

    val pastSunsetToday: Boolean = sunToday?.let { nowTime >= it.sunset } ?: false
    val pastFlipToday: Boolean = sunToday?.let {
        nowTime >= it.sunset.minus(Duration.ofMinutes(sunsetLead))
    } ?: false
    val nextSunriseLabel = if (pastSunsetToday) "Sunrise (tomorrow)" else "Sunrise"
    val nextSunriseTime = if (pastSunsetToday) sunTomorrow?.sunrise else sunToday?.sunrise
    val nextSunsetLabel = if (pastSunsetToday) "Sunset (tomorrow)" else "Sunset"
    val nextSunsetTime = if (pastSunsetToday) sunTomorrow?.sunset else sunToday?.sunset
    val flipLabel: String
    val flipValue: String?
    when {
        sunToday == null -> { flipLabel = "Flip"; flipValue = "polar — no rise/set today" }
        !pastFlipToday -> {
            val flipAt = sunToday.sunset.minus(Duration.ofMinutes(sunsetLead))
            flipLabel = "Night flip at"
            flipValue = "${flipAt.format(timeFmt)}  (sunset − ${sunsetLead}m)"
        }
        else -> {
            val nextDay = sunTomorrow?.sunrise
            flipLabel = "Day flip at"
            flipValue = if (nextDay != null) "${nextDay.format(timeFmt)}  (tomorrow's sunrise)"
                        else "tomorrow's sunrise"
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Top row: date · clock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = now.format(dateFmt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = now.format(timeFmt),
                    style = AugerLinkMonospaceMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
            // lat/lon
            DashRow(
                label = "Location",
                value = "%.3f°N  %.3f°%s  %s".format(
                    location.latitudeDeg,
                    kotlin.math.abs(location.longitudeDeg),
                    if (location.longitudeDeg >= 0) "E" else "W",
                    location.zoneId.id,
                ),
            )
            if (nextSunriseTime != null) {
                DashRow(label = nextSunriseLabel, value = nextSunriseTime.format(timeFmt))
            }
            if (nextSunsetTime != null) {
                DashRow(label = nextSunsetLabel, value = nextSunsetTime.format(timeFmt))
            }
            if (flipValue != null) {
                DashRow(label = flipLabel, value = flipValue)
            }
            // Resolved variant + mode
            DashRow(
                label = "Mode → variant",
                value = "${mode.displayName} → ${resolvedVariant.displayName}",
                valueColor = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Phase 1 location is hardcoded; Phase 6 makes it user-configurable.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun DashRow(label: String, value: String, valueColor: Color? = null) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = AugerLinkMonospaceMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
        )
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

private fun Color.toHexString(): String {
    val argb = this.value.toLong().toULong().toLong() shr 32
    val r = (argb shr 16) and 0xFF
    val g = (argb shr 8) and 0xFF
    val b = argb and 0xFF
    return "#%02X%02X%02X".format(r, g, b)
}

@Composable
private fun TokenRow(t: TokenSwatch, tokens: AugerLinkPaletteTokens) {
    val swatch = t.pick(tokens)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(swatch, RoundedCornerShape(8.dp))
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
                    text = swatch.toHexString(),
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
        FilledTonalButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
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
private fun StatusShowcase(tokens: AugerLinkPaletteTokens) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusRow(Icons.Filled.CheckCircle, tokens.success,  "Reticulum interface UP — 3 peers visible")
        StatusRow(Icons.Filled.WarningAmber, tokens.error,   "Last delivery failed — retrying in 30s")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(tokens.secondary, CircleShape),
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
private fun Footer(mode: PaletteMode, resolvedVariant: PaletteVariant) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "AugerLink 0.1.0-phase1 — palette sampler · ${mode.displayName} → ${resolvedVariant.displayName}",
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

@Preview(showBackground = true, backgroundColor = 0xFF1A1410, heightDp = 1800, name = "Night-shift")
@Composable
private fun PaletteSamplerNightPreview() {
    AugerLinkTheme(variant = PaletteVariant.Night) {
        PaletteSamplerScreen(
            mode = PaletteMode.Night,
            resolvedVariant = PaletteVariant.Night,
            location = PaletteLocation.Default,
            onModeChange = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF15181B, heightDp = 1800, name = "Day-shift")
@Composable
private fun PaletteSamplerDayPreview() {
    AugerLinkTheme(variant = PaletteVariant.Day) {
        PaletteSamplerScreen(
            mode = PaletteMode.Day,
            resolvedVariant = PaletteVariant.Day,
            location = PaletteLocation.Default,
            onModeChange = {},
        )
    }
}
