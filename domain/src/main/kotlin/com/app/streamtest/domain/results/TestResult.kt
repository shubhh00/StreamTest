package com.app.streamtest.domain.results

import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.stats.Freeze
import com.app.streamtest.domain.stats.QualityChange
import com.app.streamtest.domain.stats.StartMilestones

/** How a test ended. Failed tests are saved too: "it never started" is a result. */
enum class TestOutcome {
    /** The video started and played until the user left. */
    PLAYED,

    /** An error before the first frame (e.g. the server refused the playlist). */
    FAILED_TO_START,

    /** The video started, then an error stopped it. */
    FAILED_WHILE_PLAYING,
}

/** Whether the player chose the quality itself (adaptive bitrate) or the user forced one. */
enum class QualityMode { AUTO, FORCED }

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
    /** Null when the first frame never showed. */
    val timeToFirstFrameMs: Long?,
    val freezeCount: Int,
    val freezeTotalMs: Long,
    val droppedFrames: Int,
    /** 0..100: share of the watched time spent at 720p or sharper. */
    val timeAtHdOrBetterPercent: Int,
    val timeline: PlaybackTimeline,
    val milestones: StartMilestones,
    val outcome: TestOutcome = TestOutcome.PLAYED,
    /** Media3's error name (e.g. "ERROR_CODE_IO_BAD_HTTP_STATUS"), or null if none. */
    val errorCode: String? = null,
    /** FORCED if the user forced a quality at any point during the test. */
    val qualityMode: QualityMode = QualityMode.AUTO,
    /** The last forced quality's height (e.g. 720), or null for Auto. */
    val forcedHeight: Int? = null,
    /** Time actually watched: after the first frame, paused/background time left out. 0 = unknown (older results). */
    val testDurationMs: Long = 0,
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
