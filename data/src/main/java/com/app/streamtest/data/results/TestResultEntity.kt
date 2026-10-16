package com.app.streamtest.data.results

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row of the "test_results" table. @Entity tells Room to create a table with one
 * column per field.
 *
 * The timeline and milestones are stored as JSON text instead of extra tables.
 * That's a deliberate shortcut: we never search inside them, we only redraw them,
 * so one text column each is much simpler than two more tables and joins.
 *
 * Version 2 added outcome, errorCode, qualityMode, forcedHeight and testDurationMs,
 * and made timeToFirstFrameMs nullable (see [MIGRATION_1_2]).
 */
@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val streamId: String,
    val filmTitle: String,
    /** StreamType name: "HLS", "DASH" or "DASH_DRM". */
    val versionType: String,
    val startedAtMillis: Long,
    /** TestOutcome name: "PLAYED", "FAILED_TO_START" or "FAILED_WHILE_PLAYING". */
    val outcome: String,
    val errorCode: String?,
    /** Null when the first frame never showed. */
    val timeToFirstFrameMs: Long?,
    val freezeCount: Int,
    val freezeTotalMs: Long,
    val droppedFrames: Int,
    val timeAtHdOrBetterPercent: Int,
    /** QualityMode name: "AUTO" or "FORCED". */
    val qualityMode: String,
    val forcedHeight: Int?,
    val testDurationMs: Long,
    val timelineJson: String,
    val milestonesJson: String,
)
