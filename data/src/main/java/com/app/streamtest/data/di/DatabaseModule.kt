package com.app.streamtest.data.di

import android.content.Context
import androidx.room.Room
import com.app.streamtest.data.results.AppDatabase
import com.app.streamtest.data.results.MIGRATION_1_2
import com.app.streamtest.data.results.TestResultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// @Provides is for things Hilt can't build from a constructor, like the Room database
// (made with a builder). One database for the whole app (@Singleton).
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "streamtest.db")
            // Upgrades the table of a previous app version instead of losing saved results.
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideTestResultDao(database: AppDatabase): TestResultDao = database.testResultDao()
}
