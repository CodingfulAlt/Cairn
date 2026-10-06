package io.github.codingfulalt.cairn.core.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import io.github.codingfulalt.cairn.core.notifications.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.Clock
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Reads and writes backup files the user picks with the system file picker. Nothing leaves the phone otherwise. */
@Singleton
class BackupManager
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val habitRepository: HabitRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val reminderScheduler: ReminderScheduler,
        private val clock: Provider<Clock>,
    ) {
        /** Returns how many habits went into the file. */
        suspend fun export(uri: Uri): Int =
            withContext(Dispatchers.IO) {
                val habits = habitRepository.observeHabits().first()
                val text =
                    BackupCodec.encode(
                        habits = habits,
                        checkIns = habitRepository.observeCheckIns().first(),
                        preferences = preferencesRepository.preferences.first(),
                        exportedAt = clock.get().instant(),
                    )
                val output =
                    context.contentResolver.openOutputStream(uri, "wt")
                        ?: throw IOException("Can't write to $uri")
                output.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                habits.size
            }

        /** Replaces everything with the backup in [uri]. Returns how many habits were restored. */
        suspend fun import(uri: Uri): Int =
            withContext(Dispatchers.IO) {
                val input =
                    context.contentResolver.openInputStream(uri)
                        ?: throw IOException("Can't read $uri")
                val content = BackupCodec.decode(input.use { it.readLimited() })

                habitRepository.observeHabits().first().forEach {
                    reminderScheduler.cancelHabit(
                        it.id,
                    )
                }
                habitRepository.replaceAll(content.habits, content.checkIns)
                content.preferences?.let { prefs ->
                    preferencesRepository.setUserName(prefs.userName)
                    preferencesRepository.setThemeMode(prefs.themeMode)
                    preferencesRepository.setDailySummary(
                        prefs.dailySummaryEnabled,
                        prefs.dailySummaryTime,
                    )
                }
                reminderScheduler.rescheduleAll()
                content.habits.size
            }

        private fun InputStream.readLimited(): String {
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            var total = 0
            while (true) {
                val read = read(buffer)
                if (read < 0) break
                total += read
                if (total > BackupCodec.MAX_BYTES) throw InvalidBackupException("File is too big")
                out.write(buffer, 0, read)
            }
            return out.toString(Charsets.UTF_8.name())
        }
    }
