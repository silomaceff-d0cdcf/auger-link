package io.silomaceff.augerlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import io.silomaceff.augerlink.comms.AugerCommsRouter
import io.silomaceff.augerlink.comms.AugerLinkService
import io.silomaceff.augerlink.data.AugerLinkPrefs
import io.silomaceff.augerlink.ui.theme.AugerLinkTheme
import io.silomaceff.augerlink.ui.theme.PaletteLocation
import io.silomaceff.augerlink.ui.theme.PaletteMode
import io.silomaceff.augerlink.ui.theme.rememberResolvedVariant
import kotlinx.coroutines.launch

/**
 * Phase 3 entry point — three-tab AugerLink shell (Chats / Contacts / Settings).
 *
 * Default palette mode is [PaletteMode.Auto]: rendered variant is Day-shift
 * during daylight, flipping to Night-shift 30 minutes before local sunset
 * (NOAA Solar Calculator algorithm in `util/SunCalc.kt`). User can override
 * to fixed Day or fixed Night via the Settings tab. Override survives
 * rotation via rememberSaveable.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Start the foreground service BEFORE Reticulum init: Android filters
        // outbound IPv6 multicast egress for apps in normal-background
        // importance, so AutoInterface's announce sendto fails ~50% of the
        // time without it. The persistent notification puts the app in
        // foreground importance and lifts the filter.
        AugerLinkService.start(this)

        // Phase 4 step 1: read the user-configured TCP peer targets from
        // DataStore Preferences and pass them into the router init. Empty
        // string means "no TCP peers" (AutoInterface multicast still
        // discovers local LAN peers on its own).
        lifecycleScope.launch {
            val tcpTargets = AugerLinkPrefs.readTcpTargets(this@MainActivity)
            AugerCommsRouter.init(this@MainActivity, tcpTargets)
        }

        setContent {
            var mode by rememberSaveable { mutableStateOf(PaletteMode.Auto) }
            val location = PaletteLocation.Default
            val variant = rememberResolvedVariant(mode = mode, location = location)
            AugerLinkTheme(variant = variant) {
                AugerLinkApp(
                    paletteMode = mode,
                    resolvedVariant = variant,
                    paletteLocation = location,
                    onPaletteModeChange = { mode = it },
                )
            }
        }
    }
}
