package com.app.streamtest.data.di

import android.content.Context
import androidx.room.Room
import com.app.streamtest.data.results.AppDatabase
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
        Room.databaseBuilder(context, AppDatabase::class.java, "streamtest.db").build()

    @Provides
    fun provideTestResultDao(database: AppDatabase): TestResultDao = database.testResultDao()
}
