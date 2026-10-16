package com.app.streamtest.domain.stats

import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.results.PlaybackTimeline
import com.app.streamtest.domain.results.TestOutcome
import com.app.streamtest.domain.results.TestResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsMathTest {

    @Test
    fun `time in HD counts only 720p and up`() {
        val changes = listOf(
            QualityChange(positionMs = 0, width = 640, height = 360),      // 0–30 s: SD
            QualityChange(positionMs = 30_000, width = 1280, height = 720), // 30–60 s: HD
            QualityChange(positionMs = 60_000, width = 1920, height = 856), // 60–100 s: HD (wide 1080p)
        )
        assertEquals(70, timeAtHdOrBetterPercent(changes, playedUntilMs = 100_000))
    }

    @Test
    fun `time in HD ignores changes after the playhead`() {
        val changes = listOf(QualityChange(0, 640, 360), QualityChange(50_000, 1920, 1080))
        assertEquals(0, timeAtHdOrBetterPercent(changes, playedUntilMs = 40_000))
    }

    @Test
    fun `time in HD is 0 with nothing played`() {
        assertEquals(0, timeAtHdOrBetterPercent(emptyList(), 10_000))
        assertEquals(0, timeAtHdOrBetterPercent(listOf(QualityChange(0, 1920, 1080)), 0))
    }

    @Test
    fun `median of odd and even lists`() {
        assertEquals(400L, median(listOf(900, 100, 400)))
        assertEquals(500L, median(listOf(100, 400, 600, 2_000)))
        assertNull(median(emptyList()))
    }

    @Test
    fun `summary of saved tests`() {
        val summary = summarize(
            listOf(
                result(timeToFirstFrameMs = 1_000, freezeCount = 0, hdPercent = 100),
                result(timeToFirstFrameMs = 3_000, freezeCount = 2, hdPercent = 50),
                result(timeToFirstFrameMs = 2_000, freezeCount = 1, hdPercent = 0),
            ),
        )
        assertEquals(2_000L, summary.medianTimeToStartMs)
        assertEquals(1.0, summary.freezesPerTest, 0.001)
        assertEquals(50, summary.hdOrBetterPercent)
    }

    @Test
    fun `summary of no tests`() {
        assertEquals(ResultsSummary(null, 0.0, 0, 0), summarize(emptyList()))
    }

    @Test
    fun `HD share is weighted by watch time`() {
        // 9 minutes at 100% HD and 1 minute at 0%: 90% of the watch time was HD, not 50%.
        val summary = summarize(
            listOf(
                result(timeToFirstFrameMs = 1_000, freezeCount = 0, hdPercent = 100, watchedMs = 540_000),
                result(timeToFirstFrameMs = 1_000, freezeCount = 0, hdPercent = 0, watchedMs = 60_000),
            ),
        )
        assertEquals(90, summary.hdOrBetterPercent)
    }

    @Test
    fun `tests that never started are counted, not averaged in`() {
        val summary = summarize(
            listOf(
                result(timeToFirstFrameMs = 1_000, freezeCount = 2, hdPercent = 100, watchedMs = 10_000),
                result(timeToFirstFrameMs = null, freezeCount = 0, hdPercent = 0, watchedMs = 0, outcome = TestOutcome.FAILED_TO_START),
            ),
        )
        assertEquals(1_000L, summary.medianTimeToStartMs)
        assertEquals(2.0, summary.freezesPerTest, 0.001)
        assertEquals(100, summary.hdOrBetterPercent)
        assertEquals(1, summary.failedToStartCount)
    }

    @Test
    fun `only failures gives no median`() {
        val summary = summarize(listOf(result(null, 0, 0, outcome = TestOutcome.FAILED_TO_START)))
        assertNull(summary.medianTimeToStartMs)
        assertEquals(1, summary.failedToStartCount)
    }

    private fun result(
        timeToFirstFrameMs: Long?,
        freezeCount: Int,
        hdPercent: Int,
        watchedMs: Long = 0,
        outcome: TestOutcome = TestOutcome.PLAYED,
    ) = TestResult(
        streamId = "s",
        filmTitle = "Film",
        versionType = StreamType.HLS,
        startedAtMillis = 0,
        timeToFirstFrameMs = timeToFirstFrameMs,
        freezeCount = freezeCount,
        freezeTotalMs = 0,
        droppedFrames = 0,
        timeAtHdOrBetterPercent = hdPercent,
        timeline = PlaybackTimeline(emptyList(), emptyList(), 0, 0),
        milestones = StartMilestones(),
        outcome = outcome,
        testDurationMs = watchedMs,
    )
}
