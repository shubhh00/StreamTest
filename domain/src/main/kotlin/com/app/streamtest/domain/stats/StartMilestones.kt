package com.app.streamtest.domain.stats

/**
 * When each start-up step finished, in ms after prepare(). Null = hasn't happened (yet).
 *
 * Some steps run at the same time (e.g. the DRM license can arrive while the first
 * chunk is downloading), so these are milestones on a clock, not exact costs per step.
 */
data class StartMilestones(
    val manifestLoadedMs: Long? = null,
    val drmKeysLoadedMs: Long? = null,
    val firstVideoChunkMs: Long? = null,
    val firstFrameMs: Long? = null,
)

enum class StartStepKind { MANIFEST, DRM_LICENSE, FIRST_VIDEO_CHUNK, FIRST_FRAME }

/** One row of the "Start time, step by step" panel. */
data class StartStep(
    val kind: StartStepKind,
    /** When it happened (ms after prepare), or null. */
    val atMs: Long?,
    /** Time since the previous milestone, or null if it hasn't happened. */
    val gapMs: Long?,
)

/**
 * The steps in the order they actually happened, each with the gap from the one before.
 * Steps that haven't happened go last, in their usual order.
 */
fun StartMilestones.steps(): List<StartStep> {
    val all = listOf(
        StartStepKind.MANIFEST to manifestLoadedMs,
        StartStepKind.DRM_LICENSE to drmKeysLoadedMs,
        StartStepKind.FIRST_VIDEO_CHUNK to firstVideoChunkMs,
        StartStepKind.FIRST_FRAME to firstFrameMs,
    )
    val happened = all.filter { it.second != null }.sortedBy { it.second }
    val notYet = all.filter { it.second == null }

    var previousMs = 0L
    val happenedSteps = happened.map { (kind, atMs) ->
        val at = atMs!!
        StartStep(kind = kind, atMs = at, gapMs = at - previousMs).also { previousMs = at }
    }
    return happenedSteps + notYet.map { (kind, _) -> StartStep(kind = kind, atMs = null, gapMs = null) }
}
