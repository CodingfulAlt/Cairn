package io.github.codingfulalt.cairn.core.domain

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime

fun nextOccurrence(
    after: ZonedDateTime,
    time: LocalTime,
    days: Set<DayOfWeek>,
): ZonedDateTime? {
    if (days.isEmpty()) return null
    val start = after.toLocalDate()
    // 0..7 so "same weekday, next week" is covered when today's slot already passed
    for (offset in 0L..7L) {
        val date = start.plusDays(offset)
        if (date.dayOfWeek !in days) continue
        val candidate = ZonedDateTime.of(date, time, after.zone)
        if (candidate.isAfter(after)) return candidate
    }
    return null
}
