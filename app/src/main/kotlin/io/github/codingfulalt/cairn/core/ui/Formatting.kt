package io.github.codingfulalt.cairn.core.ui

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
@ReadOnlyComposable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

fun weekDays(locale: Locale): List<DayOfWeek> {
    val first = WeekFields.of(locale).firstDayOfWeek
    return List(7) { first.plus(it.toLong()) }
}

fun DayOfWeek.shortLabel(locale: Locale): String =
    getDisplayName(TextStyle.SHORT, locale).replaceFirstChar {
        it.titlecase(locale)
    }

fun LocalDate.fullDate(locale: Locale): String =
    format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)).replaceFirstChar {
        it.titlecase(locale)
    }

fun LocalDate.monthYear(locale: Locale): String =
    format(
        DateTimeFormatter.ofPattern("LLLL yyyy", locale),
    ).replaceFirstChar { it.titlecase(locale) }

fun formatTime(
    context: Context,
    time: LocalTime,
): String {
    val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
    return time.format(
        DateTimeFormatter.ofPattern(pattern, context.resources.configuration.locales[0]),
    )
}

@Composable
fun formatTime(time: LocalTime): String = formatTime(LocalContext.current, time)

@Composable
fun scheduleLabel(schedule: Set<DayOfWeek>): String {
    val locale = currentLocale()
    return when (schedule) {
        Habit.EVERY_DAY -> stringResource(R.string.schedule_every_day)
        Habit.WEEKDAYS -> stringResource(R.string.schedule_weekdays)
        Habit.WEEKENDS -> stringResource(R.string.schedule_weekends)
        else ->
            weekDays(
                locale,
            ).filter { it in schedule }.joinToString(" · ") { it.shortLabel(locale) }
    }
}

@Composable
fun greeting(hour: Int): String =
    stringResource(
        when (hour) {
            in 5..11 -> R.string.greeting_morning
            in 12..17 -> R.string.greeting_afternoon
            in 18..22 -> R.string.greeting_evening
            else -> R.string.greeting_night
        },
    )

@Composable
fun HabitColor.label(): String =
    stringResource(
        when (this) {
            HabitColor.Moss -> R.string.color_moss
            HabitColor.Mint -> R.string.color_mint
            HabitColor.Sky -> R.string.color_sky
            HabitColor.Lavender -> R.string.color_lavender
            HabitColor.Rose -> R.string.color_rose
            HabitColor.Peach -> R.string.color_peach
            HabitColor.Sand -> R.string.color_sand
            HabitColor.Coral -> R.string.color_coral
        },
    )
