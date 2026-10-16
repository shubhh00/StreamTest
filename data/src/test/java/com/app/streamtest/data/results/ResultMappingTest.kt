package com.app.streamtest.data.results

import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.results.PlaybackTimeline
import com.app.streamtest.domain.results.QualityMode
import com.app.streamtest.domain.results.TestOutcome
import com.app.streamtest.domain.results.TestResult
import com.app.streamtest.domain.stats.Freeze
import com.app.streamtest.domain.stats.QualityChange
import com.app.streamtest.domain.stats.StartMilestones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResultMappingTest {

    @Test
    fun `result survives the trip to a database row and back`() {
        val result = TestResult(
            id = 7,
            streamId = "tears-drm",
            filmTitle = "Tears of Steel",
            versionType = StreamType.DASH_DRM,
            startedAtMillis = 1_760_000_000_000,
            timeToFirstFrameMs = 2_774,
            freezeCount = 1,
            freezeTotalMs = 900,
            droppedFrames = 3,
            timeAtHdOrBetterPercent = 62,
            timeline = PlaybackTimeline(
                qualityChanges = listOf(QualityChange(0, 854, 380), QualityChange(14_000, 1920, 856)),
                freezes = listOf(Freeze(30_000, 900)),
                playedUntilMs = 60_000,
                lengthMs = 734_000,
            ),
            milestones = StartMilestones(manifestLoadedMs = 704, drmKeysLoadedMs = 1_778, firstFrameMs = 2_774),
        )

        val row = result.toEntity()
        assertEquals("DASH_DRM", row.versionType)
        assertEquals(result, row.toTestResult())
    }

    @Test
    fun `a test that never started is saved with its outcome and error`() {
        val result = TestResult(
            streamId = "live",
            filmTitle = "Live stream",
            versionType = StreamType.HLS,
            startedAtMillis = 1_760_000_000_000,
            timeToFirstFrameMs = null,
            freezeCount = 0,
            freezeTotalMs = 0,
            droppedFrames = 0,
            timeAtHdOrBetterPercent = 0,
            timeline = PlaybackTimeline(emptyList(), emptyList(), 0, 0),
            milestones = StartMilestones(manifestLoadedMs = 900),
            outcome = TestOutcome.FAILED_TO_START,
            errorCode = "ERROR_CODE_IO_BAD_HTTP_STATUS",
            qualityMode = QualityMode.FORCED,
            forcedHeight = 720,
            testDurationMs = 0,
        )

        val row = result.toEntity()
        assertEquals("FAILED_TO_START", row.outcome)
        assertNull(row.timeToFirstFrameMs)
        assertEquals(result, row.toTestResult())
    }
}
