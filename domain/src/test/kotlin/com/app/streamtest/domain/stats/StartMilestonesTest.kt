package com.app.streamtest.domain.stats

import org.junit.Assert.assertEquals
import org.junit.Test

class StartMilestonesTest {

    @Test
    fun `clear stream has no DRM step, gaps add up to the total`() {
        val steps = StartMilestones(manifestLoadedMs = 400, firstVideoChunkMs = 1_300, firstFrameMs = 1_600).steps()

        assertEquals(
            listOf(
                StartStep(StartStepKind.MANIFEST, 400, 400),
                StartStep(StartStepKind.FIRST_VIDEO_CHUNK, 1_300, 900),
                StartStep(StartStepKind.FIRST_FRAME, 1_600, 300),
                StartStep(StartStepKind.DRM_LICENSE, null, null),
            ),
            steps,
        )
        assertEquals(1_600L, steps.mapNotNull { it.gapMs }.sum())
    }

    @Test
    fun `steps are shown in the order they happened`() {
        // The license arrived after the first chunk: it ran in parallel.
        val steps = StartMilestones(
            manifestLoadedMs = 300,
            drmKeysLoadedMs = 1_500,
            firstVideoChunkMs = 1_000,
            firstFrameMs = 1_700,
        ).steps()

        assertEquals(
            listOf(StartStepKind.MANIFEST, StartStepKind.FIRST_VIDEO_CHUNK, StartStepKind.DRM_LICENSE, StartStepKind.FIRST_FRAME),
            steps.map { it.kind },
        )
        assertEquals(listOf(300L, 700L, 500L, 200L), steps.map { it.gapMs })
    }

    @Test
    fun `nothing happened yet`() {
        val steps = StartMilestones().steps()
        assertEquals(4, steps.size)
        assertEquals(listOf(null, null, null, null), steps.map { it.atMs })
    }
}
