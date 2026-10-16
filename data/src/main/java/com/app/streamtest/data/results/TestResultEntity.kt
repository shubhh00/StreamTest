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
 */
@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val streamId: String,
    val filmTitle: String,
    /** StreamType name: "HLS", "DASH" or "DASH_DRM". */
    val versionType: String,
    val startedAtMillis: Long,
    val timeToFirstFrameMs: Long,
    val freezeCount: Int,
    val freezeTotalMs: Long,
    val droppedFrames: Int,
    val timeAtHdOrBetterPercent: Int,
    val timelineJson: String,
    val milestonesJson: String,
)
