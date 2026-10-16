package com.app.streamtest.domain.stats

import com.app.streamtest.domain.results.TestOutcome
import com.app.streamtest.domain.results.TestResult

/** "HD or better" = 720p and up. */
private const val HD_HEIGHT = 720

/**
 * Share of the watched time (0..100) spent at 720p or sharper.
 * Each quality lasts from its change until the next change (or [playedUntilMs]).
 */
fun timeAtHdOrBetterPercent(changes: List<QualityChange>, playedUntilMs: Long): Int {
    if (playedUntilMs <= 0 || changes.isEmpty()) return 0
    val sorted = changes.sortedBy { it.positionMs }
    var hdMs = 0L
    sorted.forEachIndexed { index, change ->
        val startMs = if (index == 0) 0L else change.positionMs
        val endMs = minOf(sorted.getOrNull(index + 1)?.positionMs ?: playedUntilMs, playedUntilMs)
        if (endMs > startMs && standardHeight(change.width, change.height) >= HD_HEIGHT) {
            hdMs += endMs - startMs
        }
    }
    return (hdMs * 100 / playedUntilMs).toInt()
}

/** The middle value (or the average of the two middle values). Null for an empty list. */
fun median(values: List<Long>): Long? {
    if (values.isEmpty()) return null
    val sorted = values.sorted()
    val middle = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[middle] else (sorted[middle - 1] + sorted[middle]) / 2
}

/** The one-line summary at the top of the Log. */
data class ResultsSummary(
    /** Over the tests that started. Null if none did. */
    val medianTimeToStartMs: Long?,
    /** Over the tests that started. */
    val freezesPerTest: Double,
    /** Share of all watched time spent in HD: a 10-minute test counts 10 times a 1-minute one. */
    val hdOrBetterPercent: Int,
    val failedToStartCount: Int = 0,
)

fun summarize(results: List<TestResult>): ResultsSummary {
    val started = results.filter { it.timeToFirstFrameMs != null }
    val failedToStart = results.count { it.outcome == TestOutcome.FAILED_TO_START }
    if (started.isEmpty()) return ResultsSummary(null, 0.0, 0, failedToStart)

    // Weight each test's HD share by how long it was watched. Older results have no
    // test duration, so fall back to how far into the video they got.
    val weights = started.map { if (it.testDurationMs > 0) it.testDurationMs else it.timeline.playedUntilMs }
    val totalWeight = weights.sum()
    val hdPercent = if (totalWeight > 0) {
        (started.zip(weights).sumOf { (result, weight) -> result.timeAtHdOrBetterPercent * weight } / totalWeight).toInt()
    } else {
        // No watch times known at all: a plain average is the best we can do.
        started.sumOf { it.timeAtHdOrBetterPercent } / started.size
    }
    return ResultsSummary(
        // Median, not average: one very slow start shouldn't drag the number around.
        medianTimeToStartMs = median(started.mapNotNull { it.timeToFirstFrameMs }),
        freezesPerTest = started.sumOf { it.freezeCount }.toDouble() / started.size,
        hdOrBetterPercent = hdPercent,
        failedToStartCount = failedToStart,
    )
}
