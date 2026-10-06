package io.github.codingfulalt.cairn

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.codingfulalt.cairn.core.common.di.ApplicationScope
import io.github.codingfulalt.cairn.core.notifications.Notifier
import io.github.codingfulalt.cairn.core.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CairnApplication : Application() {
    @Inject lateinit var notifier: Notifier

    @Inject lateinit var reminderScheduler: ReminderScheduler

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        notifier.createChannels()
        // cheap, and it heals alarms if the OS dropped them
        applicationScope.launch { reminderScheduler.rescheduleAll() }
    }
}
