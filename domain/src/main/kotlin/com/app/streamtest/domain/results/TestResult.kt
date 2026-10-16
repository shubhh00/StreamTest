package com.app.streamtest.domain.results

import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.stats.Freeze
import com.app.streamtest.domain.stats.QualityChange
import com.app.streamtest.domain.stats.StartMilestones

/** One saved test: what played, how fast it started, and how it went. */
data class TestResult(
    /** 0 for a result that hasn't been saved yet; the database picks the real id. */
    val id: Long = 0,
    val streamId: String,
    /** The film title, or the extra stream's title. */
    val filmTitle: String,
    val versionType: StreamType,
    /** When the test started (wall clock, ms since 1970). */
    val startedAtMillis: Long,
    val timeToFirstFrameMs: Long,
    val freezeCount: Int,
    val freezeTotalMs: Long,
    val droppedFrames: Int,
    /** 0..100: share of the watched time spent at 720p or sharper. */
    val timeAtHdOrBetterPercent: Int,
    val timeline: PlaybackTimeline,
    val milestones: StartMilestones,
)

/** What's needed to redraw this test's quality line later (in the Log). */
data class PlaybackTimeline(
    val qualityChanges: List<QualityChange>,
    val freezes: List<Freeze>,
    /** How far the user watched. */
    val playedUntilMs: Long,
    /** Full length of the line: the video's duration, or time watched for live streams. */
    val lengthMs: Long,
)
