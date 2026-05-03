package io.silomaceff.augerlink.ui.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Format helpers for chat timestamps. Matches the conversational style
 * users expect from Signal / WhatsApp / Sideband:
 *  - <1 min:    "now"
 *  - <60 min:   "12 min"
 *  - today:     "14:32"
 *  - yesterday: "Yesterday 14:32"
 *  - this week: "Wed 14:32"
 *  - older:     "May 1"
 */
object TimeFormat {
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val shortDateFmt = DateTimeFormatter.ofPattern("MMM d")
    private val dayOfWeekFmt = DateTimeFormatter.ofPattern("EEE")

    fun shortRelative(when_: Instant, now: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()): String {
        val gap = Duration.between(when_, now).toMinutes()
        if (gap < 1) return "now"
        if (gap < 60) return "$gap min"
        val whenLocal = when_.atZone(zone)
        val nowLocal = now.atZone(zone)
        val whenDate = whenLocal.toLocalDate()
        val nowDate = nowLocal.toLocalDate()
        return when {
            whenDate == nowDate -> whenLocal.toLocalTime().format(timeFmt)
            whenDate == nowDate.minusDays(1) -> "Yesterday ${whenLocal.toLocalTime().format(timeFmt)}"
            ChronoUnit.DAYS.between(whenDate, nowDate) < 7 ->
                "${whenLocal.format(dayOfWeekFmt)} ${whenLocal.toLocalTime().format(timeFmt)}"
            else -> whenLocal.format(shortDateFmt)
        }
    }

    fun timeOnly(when_: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        when_.atZone(zone).toLocalTime().format(timeFmt)

    fun dateBucket(when_: Instant, zone: ZoneId = ZoneId.systemDefault(), now: Instant = Instant.now()): String {
        val date = when_.atZone(zone).toLocalDate()
        val today = LocalDate.ofInstant(now, zone)
        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            ChronoUnit.DAYS.between(date, today) < 7 -> date.format(dayOfWeekFmt)
            else -> date.format(shortDateFmt)
        }
    }
}
