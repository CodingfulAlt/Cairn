package io.github.codingfulalt.cairn.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.codingfulalt.cairn.core.database.CairnDatabase
import io.github.codingfulalt.cairn.core.database.dao.CheckInDao
import io.github.codingfulalt.cairn.core.database.dao.HabitDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): CairnDatabase = Room.databaseBuilder(context, CairnDatabase::class.java, "cairn.db").build()

    @Provides
    fun provideHabitDao(database: CairnDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideCheckInDao(database: CairnDatabase): CheckInDao = database.checkInDao()
}
