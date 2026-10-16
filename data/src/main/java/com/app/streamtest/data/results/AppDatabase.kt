package com.app.streamtest.data.results

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The app's SQLite database. Room generates the implementation.
 * exportSchema = false: we don't keep schema JSON files, since there's only one
 * version so far. When the table changes, bump the version and add a migration.
 */
@Database(entities = [TestResultEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testResultDao(): TestResultDao
}
