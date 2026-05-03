package io.silomaceff.augerlink.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sunrise / sunset calculator — NOAA Solar Calculator algorithm.
 *
 * Pure Kotlin, no dependencies. Accurate to ~1 minute for mid-latitudes.
 * Returns null in polar regions on days with no sunrise or no sunset.
 *
 * Reference: https://gml.noaa.gov/grad/solcalc/calcdetails.html
 *            https://en.wikipedia.org/wiki/Sunrise_equation
 */
object SunCalc {

    data class SunTimes(val sunrise: LocalTime, val sunset: LocalTime)

    /**
     * @param date local date for which to compute sun times
     * @param latitudeDeg latitude in decimal degrees (north positive)
     * @param longitudeDeg longitude in decimal degrees (east positive)
     * @param zoneId time zone for the returned [LocalTime]s
     * @return sunrise + sunset, or null if the sun does not rise/set on this date at this location
     */
    fun sunriseSunset(
        date: LocalDate,
        latitudeDeg: Double,
        longitudeDeg: Double,
        zoneId: ZoneId,
    ): SunTimes? {
        // Days since J2000.0 for the date's local noon (offset by longitude/360).
        val n = (date.toEpochDay() + 2440588.0 - 2451545.0) + 0.0008
        val Jstar = n - longitudeDeg / 360.0

        val M = (357.5291 + 0.98560028 * Jstar) % 360.0
        val Mrad = Math.toRadians(M)

        val C = 1.9148 * sin(Mrad) + 0.0200 * sin(2 * Mrad) + 0.0003 * sin(3 * Mrad)
        val lambda = (M + C + 180.0 + 102.9372) % 360.0
        val lambdaRad = Math.toRadians(lambda)

        val Jtransit = 2451545.0 + Jstar + 0.0053 * sin(Mrad) - 0.0069 * sin(2 * lambdaRad)

        val sinDelta = sin(lambdaRad) * sin(Math.toRadians(23.4397))
        val cosDelta = cos(asin(sinDelta))

        val latRad = Math.toRadians(latitudeDeg)
        val cosH0 = (sin(Math.toRadians(-0.83)) - sin(latRad) * sinDelta) / (cos(latRad) * cosDelta)
        if (cosH0 > 1.0 || cosH0 < -1.0) return null

        val H0 = Math.toDegrees(acos(cosH0))
        val Jrise = Jtransit - H0 / 360.0
        val Jset = Jtransit + H0 / 360.0

        return SunTimes(
            sunrise = julianToLocalTime(Jrise, zoneId),
            sunset = julianToLocalTime(Jset, zoneId),
        )
    }

    private fun julianToLocalTime(julian: Double, zoneId: ZoneId): LocalTime {
        val unixSeconds = (julian - 2440587.5) * 86400.0
        val instant = Instant.ofEpochMilli((unixSeconds * 1000.0).toLong())
        return instant.atZone(zoneId).toLocalTime()
    }
}
