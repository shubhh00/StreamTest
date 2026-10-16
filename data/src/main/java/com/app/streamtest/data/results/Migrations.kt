package com.app.streamtest.data.results

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * A Migration tells Room how to turn the old table into the new one, so results
 * saved by the previous version of the app survive the update.
 *
 * Version 1 → 2: adds outcome, errorCode, qualityMode, forcedHeight, testDurationMs,
 * and makes timeToFirstFrameMs nullable. SQLite can't change a column from NOT NULL to
 * nullable in place, so we build the new table, copy every row across, and swap.
 * Old rows get: outcome PLAYED (only played tests were saved before), quality AUTO
 * (not recorded then), and testDurationMs 0 (= unknown).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE test_results_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                streamId TEXT NOT NULL,
                filmTitle TEXT NOT NULL,
                versionType TEXT NOT NULL,
                startedAtMillis INTEGER NOT NULL,
                outcome TEXT NOT NULL,
                errorCode TEXT,
                timeToFirstFrameMs INTEGER,
                freezeCount INTEGER NOT NULL,
                freezeTotalMs INTEGER NOT NULL,
                droppedFrames INTEGER NOT NULL,
                timeAtHdOrBetterPercent INTEGER NOT NULL,
                qualityMode TEXT NOT NULL,
                forcedHeight INTEGER,
                testDurationMs INTEGER NOT NULL,
                timelineJson TEXT NOT NULL,
                milestonesJson TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO test_results_new (
                id, streamId, filmTitle, versionType, startedAtMillis, outcome, errorCode,
                timeToFirstFrameMs, freezeCount, freezeTotalMs, droppedFrames, timeAtHdOrBetterPercent,
                qualityMode, forcedHeight, testDurationMs, timelineJson, milestonesJson
            )
            SELECT
                id, streamId, filmTitle, versionType, startedAtMillis, 'PLAYED', NULL,
                timeToFirstFrameMs, freezeCount, freezeTotalMs, droppedFrames, timeAtHdOrBetterPercent,
                'AUTO', NULL, 0, timelineJson, milestonesJson
            FROM test_results
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE test_results")
        db.execSQL("ALTER TABLE test_results_new RENAME TO test_results")
    }
}
