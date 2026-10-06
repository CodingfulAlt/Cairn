package io.github.codingfulalt.cairn.core.backup

import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.model.HabitIcon
import io.github.codingfulalt.cairn.core.model.ThemeMode
import io.github.codingfulalt.cairn.core.model.UserPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class InvalidBackupException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

data class BackupContent(
    val habits: List<Habit>,
    val checkIns: List<CheckIn>,
    val preferences: UserPreferences?,
)

/** Turns habits and check-ins into a JSON backup and back, checking everything on the way in. */
object BackupCodec {
    const val FORMAT = "cairn-backup"
    const val VERSION = 1

    // a real backup is a few hundred KB at most, anything bigger is not ours
    const val MAX_BYTES = 5 * 1024 * 1024

    private const val MAX_USER_NAME = 40

    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
        }

    fun encode(
        habits: List<Habit>,
        checkIns: List<CheckIn>,
        preferences: UserPreferences,
        exportedAt: Instant,
    ): String =
        json.encodeToString(
            BackupFile.serializer(),
            BackupFile(
                format = FORMAT,
                version = VERSION,
                exportedAt = exportedAt.toString(),
                habits = habits.map { it.toBackup() },
                checkIns = checkIns.map { BackupCheckIn(it.habitId, it.date.toString(), it.count) },
                preferences = preferences.toBackup(),
            ),
        )

    fun decode(text: String): BackupContent {
        val file =
            try {
                json.decodeFromString(BackupFile.serializer(), text)
            } catch (e: Exception) {
                throw InvalidBackupException("Not a Cairn backup", e)
            }
        if (file.format != FORMAT) throw InvalidBackupException("Unknown format: ${file.format}")
        if (file.version > VERSION) throw InvalidBackupException("Made by a newer version of Cairn")

        val habits = file.habits.mapNotNull { it.toHabit() }.distinctBy { it.id }
        val goals = habits.associate { it.id to it.dailyGoal }
        val checkIns =
            file.checkIns
                .mapNotNull { checkIn ->
                    val goal = goals[checkIn.habitId] ?: return@mapNotNull null
                    val date = checkIn.date.toDateOrNull() ?: return@mapNotNull null
                    if (checkIn.count <= 0) return@mapNotNull null
                    CheckIn(checkIn.habitId, date, checkIn.count.coerceAtMost(goal))
                }.distinctBy { it.habitId to it.date }
        return BackupContent(habits, checkIns, file.preferences?.toModel())
    }

    private fun Habit.toBackup() =
        BackupHabit(
            id = id,
            name = name,
            description = description,
            icon = icon.key,
            color = color.key,
            dailyGoal = dailyGoal,
            schedule = schedule.map { it.value }.sorted(),
            reminder = reminder?.toString(),
            createdOn = createdOn.toString(),
            archived = archived,
            sortOrder = sortOrder,
        )

    private fun BackupHabit.toHabit(): Habit? {
        val cleanName = name.trim().take(Habit.MAX_NAME_LENGTH)
        val created = createdOn.toDateOrNull()
        if (id <= 0 || cleanName.isEmpty() || created == null) return null
        val days = schedule.mapNotNull { day -> DayOfWeek.entries.getOrNull(day - 1) }.toSet()
        return Habit(
            id = id,
            name = cleanName,
            description = description.trim().take(Habit.MAX_NOTE_LENGTH),
            icon = HabitIcon.fromKey(icon),
            color = HabitColor.fromKey(color),
            dailyGoal = dailyGoal.coerceIn(1, Habit.MAX_DAILY_GOAL),
            schedule = days.ifEmpty { Habit.EVERY_DAY },
            reminder = reminder?.toTimeOrNull(),
            createdOn = created,
            archived = archived,
            sortOrder = sortOrder,
        )
    }

    private fun UserPreferences.toBackup() =
        BackupPreferences(
            userName = userName,
            themeMode = themeMode.name,
            dailySummaryEnabled = dailySummaryEnabled,
            dailySummaryTime = dailySummaryTime.toString(),
        )

    private fun BackupPreferences.toModel() =
        UserPreferences(
            themeMode = ThemeMode.entries.firstOrNull { it.name == themeMode } ?: ThemeMode.System,
            userName = userName.trim().take(MAX_USER_NAME),
            onboardingCompleted = true,
            dailySummaryEnabled = dailySummaryEnabled,
            dailySummaryTime =
                dailySummaryTime.toTimeOrNull() ?: UserPreferences.DEFAULT_SUMMARY_TIME,
        )

    private fun String.toDateOrNull(): LocalDate? =
        runCatching { LocalDate.parse(this) }.getOrNull()

    private fun String.toTimeOrNull(): LocalTime? =
        runCatching { LocalTime.parse(this) }.getOrNull()
}

@Serializable
internal data class BackupFile(
    val format: String,
    val version: Int,
    val exportedAt: String = "",
    val habits: List<BackupHabit> = emptyList(),
    val checkIns: List<BackupCheckIn> = emptyList(),
    val preferences: BackupPreferences? = null,
)

@Serializable
internal data class BackupHabit(
    val id: Long,
    val name: String,
    val description: String = "",
    val icon: String = "",
    val color: String = "",
    val dailyGoal: Int = 1,
    // ISO days, 1 = Monday
    val schedule: List<Int> = emptyList(),
    val reminder: String? = null,
    val createdOn: String,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
)

@Serializable
internal data class BackupCheckIn(
    val habitId: Long,
    val date: String,
    val count: Int,
)

@Serializable
internal data class BackupPreferences(
    val userName: String = "",
    val themeMode: String = "",
    val dailySummaryEnabled: Boolean = false,
    val dailySummaryTime: String = "",
)
