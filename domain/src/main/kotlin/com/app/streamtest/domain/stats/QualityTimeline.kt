package com.app.streamtest.domain.stats

import kotlin.math.abs

/** How sharp the picture is, for coloring the quality line. Brighter = sharper. */
enum class QualityBucket { LOW, MID, HIGH }

/** The video quality changed at this point of the video. */
data class QualityChange(val positionMs: Long, val width: Int, val height: Int)

/** The video froze (stopped to buffer) at this point, for this long. */
data class Freeze(val positionMs: Long, val durationMs: Long)

/** One colored piece of the quality line, as fractions (0..1) of its width. */
data class QualitySegment(val startFraction: Float, val endFraction: Float, val bucket: QualityBucket)

/** Everything needed to draw a quality line. */
data class QualityLineModel(
    val segments: List<QualitySegment> = emptyList(),
    val freezeFractions: List<Float> = emptyList(),
)

private val standardHeights = listOf(144, 240, 360, 480, 576, 720, 1080, 1440, 2160)

/**
 * The usual name of a resolution ("1080p"), as a height.
 * Films are often wider than 16:9 (e.g. 1920×856), so we look at the width too, then
 * snap to the nearest standard height: 1920×856 → 1080, 784×350 → 480.
 */
fun standardHeight(width: Int, height: Int): Int {
    val effectiveHeight = maxOf(height, width * 9 / 16)
    return standardHeights.minBy { abs(it - effectiveHeight) }
}

fun qualityBucket(width: Int, height: Int): QualityBucket {
    val standard = standardHeight(width, height)
    return when {
        standard >= 1080 -> QualityBucket.HIGH
        standard >= 720 -> QualityBucket.MID
        else -> QualityBucket.LOW
    }
}

/**
 * Turns the recorded quality changes and freezes into something to draw.
 * Each quality lasts from its change until the next change (or until [playedUntilMs]).
 * Only the played part gets colored; the rest of the line stays empty.
 */
fun buildQualityLine(
    changes: List<QualityChange>,
    freezes: List<Freeze>,
    playedUntilMs: Long,
    durationMs: Long,
): QualityLineModel {
    if (durationMs <= 0) return QualityLineModel()
    val sorted = changes.sortedBy { it.positionMs }

    val segments = sorted.mapIndexedNotNull { index, change ->
        // The first quality starts the line, even if its event arrived a few ms after 0.
        val startMs = if (index == 0) 0L else change.positionMs
        val endMs = sorted.getOrNull(index + 1)?.positionMs ?: playedUntilMs
        val clippedEndMs = minOf(endMs, playedUntilMs)
        if (clippedEndMs <= startMs) {
            null
        } else {
            QualitySegment(
                startFraction = fraction(startMs, durationMs),
                endFraction = fraction(clippedEndMs, durationMs),
                bucket = qualityBucket(change.width, change.height),
            )
        }
    }
    return QualityLineModel(
        segments = segments,
        freezeFractions = freezes.map { fraction(it.positionMs, durationMs) },
    )
}

private fun fraction(positionMs: Long, durationMs: Long): Float =
    (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
