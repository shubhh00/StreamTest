package com.app.streamtest.domain.stats

import org.junit.Assert.assertEquals
import org.junit.Test

class QualityTimelineTest {

    @Test
    fun `wide films count by width`() {
        assertEquals(1080, standardHeight(1920, 856))
        assertEquals(480, standardHeight(784, 350))
        assertEquals(2160, standardHeight(3840, 2160))
        assertEquals(QualityBucket.HIGH, qualityBucket(1920, 856))
        assertEquals(QualityBucket.MID, qualityBucket(1280, 720))
        assertEquals(QualityBucket.LOW, qualityBucket(640, 360))
    }

    @Test
    fun `one quality colors the played part only`() {
        val line = buildQualityLine(
            changes = listOf(QualityChange(positionMs = 40, width = 1280, height = 720)),
            freezes = emptyList(),
            playedUntilMs = 25_000,
            durationMs = 100_000,
        )
        assertEquals(listOf(QualitySegment(0f, 0.25f, QualityBucket.MID)), line.segments)
    }

    @Test
    fun `quality change splits the line`() {
        val line = buildQualityLine(
            changes = listOf(
                QualityChange(positionMs = 30_000, width = 1920, height = 1080),
                QualityChange(positionMs = 0, width = 640, height = 360),
            ),
            freezes = emptyList(),
            playedUntilMs = 50_000,
            durationMs = 100_000,
        )
        assertEquals(
            listOf(
                QualitySegment(0f, 0.3f, QualityBucket.LOW),
                QualitySegment(0.3f, 0.5f, QualityBucket.HIGH),
            ),
            line.segments,
        )
    }

    @Test
    fun `change after the playhead is not drawn yet`() {
        val line = buildQualityLine(
            changes = listOf(QualityChange(0, 640, 360), QualityChange(60_000, 1920, 1080)),
            freezes = emptyList(),
            playedUntilMs = 50_000,
            durationMs = 100_000,
        )
        assertEquals(listOf(QualitySegment(0f, 0.5f, QualityBucket.LOW)), line.segments)
    }

    @Test
    fun `freezes become dots`() {
        val line = buildQualityLine(
            changes = emptyList(),
            freezes = listOf(Freeze(positionMs = 10_000, durationMs = 800), Freeze(positionMs = 75_000, durationMs = 300)),
            playedUntilMs = 80_000,
            durationMs = 100_000,
        )
        assertEquals(listOf(0.1f, 0.75f), line.freezeFractions)
    }

    @Test
    fun `unknown duration gives an empty line`() {
        assertEquals(QualityLineModel(), buildQualityLine(listOf(QualityChange(0, 640, 360)), emptyList(), 1_000, 0))
    }
}
