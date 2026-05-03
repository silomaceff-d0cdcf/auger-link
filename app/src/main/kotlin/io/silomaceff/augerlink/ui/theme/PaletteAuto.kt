package io.silomaceff.augerlink.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.silomaceff.augerlink.util.SunCalc
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * User-selectable palette mode. Distinct from [PaletteVariant], which is
 * the rendered (resolved) palette. Auto resolves to Day or Night based on
 * the current time + location's sunrise/sunset.
 */
enum class PaletteMode(val displayName: String) {
    Auto("Auto"),
    Day("Day-shift"),
    Night("Night-shift"),
}

/**
 * Location used by [rememberResolvedVariant] when [PaletteMode.Auto] is active.
 * Phase 1 hardcodes a sensible mid-latitude default. Phase 6 surfaces
 * user-set lat/lon (manual entry, no permission prompt) plus optional
 * coarse-location permission for true auto-detect.
 */
data class PaletteLocation(
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    companion object {
        // Central Massachusetts — north-by-northeast US. Reasonable default
        // for the dev region; user-configurable in Phase 6.
        val Default = PaletteLocation(latitudeDeg = 42.5, longitudeDeg = -71.5)
    }
}

/** Minutes BEFORE sunset to flip to Night-shift. Configurable; default 30. */
const val SUNSET_LEAD_MINUTES: Long = 30

/**
 * Resolves [PaletteMode] to the actual rendered [PaletteVariant].
 *
 * Auto mode logic:
 *  - Compute today's local sunrise + sunset via [SunCalc] for [location].
 *  - effective_sunset = sunset - [SUNSET_LEAD_MINUTES] minutes.
 *  - if currentTime in [sunrise, effective_sunset) → Day
 *  - else → Night.
 *
 * Re-checks every 60 seconds when in Auto mode, so the variant flips live
 * as the threshold passes.
 */
@Composable
fun rememberResolvedVariant(
    mode: PaletteMode,
    location: PaletteLocation = PaletteLocation.Default,
): PaletteVariant {
    if (mode != PaletteMode.Auto) {
        return when (mode) {
            PaletteMode.Day -> PaletteVariant.Day
            PaletteMode.Night -> PaletteVariant.Night
            PaletteMode.Auto -> error("unreachable")
        }
    }
    val variant by produceState(
        initialValue = computeAutoVariantNow(location),
        key1 = location,
    ) {
        while (isActive) {
            value = computeAutoVariantNow(location)
            delay(60_000L)
        }
    }
    return variant
}

private fun computeAutoVariantNow(location: PaletteLocation): PaletteVariant {
    val now = java.time.ZonedDateTime.now(location.zoneId)
    val today = now.toLocalDate()
    val nowTime = now.toLocalTime()

    val sunTimes = SunCalc.sunriseSunset(
        date = today,
        latitudeDeg = location.latitudeDeg,
        longitudeDeg = location.longitudeDeg,
        zoneId = location.zoneId,
    ) ?: return PaletteVariant.Night  // polar night/day fallback — go conservative

    val effectiveSunset: LocalTime =
        sunTimes.sunset.minus(Duration.ofMinutes(SUNSET_LEAD_MINUTES))

    val isDay = nowTime >= sunTimes.sunrise && nowTime < effectiveSunset
    return if (isDay) PaletteVariant.Day else PaletteVariant.Night
}

/** Convenience for the sampler footer / status row — when in Auto mode,
 *  the user wants to see WHY it picked the current variant. */
fun describeAutoState(location: PaletteLocation = PaletteLocation.Default): String {
    val now = java.time.ZonedDateTime.now(location.zoneId)
    val today = now.toLocalDate()
    val sunTimes = SunCalc.sunriseSunset(
        date = today,
        latitudeDeg = location.latitudeDeg,
        longitudeDeg = location.longitudeDeg,
        zoneId = location.zoneId,
    ) ?: return "Auto: polar fallback (no sunrise/sunset today)"
    val flip = sunTimes.sunset.minus(Duration.ofMinutes(SUNSET_LEAD_MINUTES))
    return "Auto: Day until %s, Night thereafter (sunset %s)".format(
        flip.withSecond(0).withNano(0),
        sunTimes.sunset.withSecond(0).withNano(0),
    )
}
