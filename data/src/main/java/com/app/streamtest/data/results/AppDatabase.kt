package com.app.streamtest.data.results

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The app's SQLite database. Room generates the implementation.
 * When the table changes, bump the version and add a Migration (see Migrations.kt).
 * exportSchema = false: we don't keep schema JSON files; the migration SQL is the record.
 */
@Database(entities = [TestResultEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testResultDao(): TestResultDao
}
