package com.app.streamtest.domain.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSessionTest {

    @Test
    fun `watched time starts at the first frame`() {
        val session = PlaybackSession()
        session.onFirstFrame(nowMs = 2_000)
        assertEquals(8_000L, session.watchedMs(nowMs = 10_000))
    }

    @Test
    fun `time in the background is not counted`() {
        val session = PlaybackSession()
        session.onFirstFrame(nowMs = 0)
        session.onWantsToPlayChanged(false, positionMs = 10_000, nowMs = 10_000) // HOME
        session.onWantsToPlayChanged(true, positionMs = 10_000, nowMs = 70_000) // back a minute later
        assertEquals(15_000L, session.watchedMs(nowMs = 75_000))
    }

    @Test
    fun `a freeze still going on exit is saved with its duration`() {
        val session = PlaybackSession()
        session.onFirstFrame(nowMs = 0)
        session.onBufferingChanged(true, positionMs = 30_000, nowMs = 30_000)

        assertEquals(listOf(Freeze(30_000, 2_000)), session.freezes(nowMs = 32_000)) // shown live
        session.finish(nowMs = 35_000) // BACK while still buffering
        assertEquals(listOf(Freeze(30_000, 5_000)), session.freezes(nowMs = 99_000))
    }

    @Test
    fun `pausing ends a freeze and pause time is not freeze time`() {
        val session = PlaybackSession()
        session.onFirstFrame(nowMs = 0)
        session.onBufferingChanged(true, positionMs = 5_000, nowMs = 5_000)
        session.onWantsToPlayChanged(false, positionMs = 5_000, nowMs = 6_000) // HOME during the freeze
        session.onWantsToPlayChanged(true, positionMs = 5_000, nowMs = 60_000) // back, still buffering
        session.onBufferingChanged(false, positionMs = 5_000, nowMs = 61_000)

        assertEquals(listOf(Freeze(5_000, 1_000), Freeze(5_000, 1_000)), session.freezes(nowMs = 61_000))
    }

    @Test
    fun `buffering before the first frame is start-up, not a freeze`() {
        val session = PlaybackSession()
        session.onBufferingChanged(true, positionMs = 0, nowMs = 0)
        session.onBufferingChanged(false, positionMs = 0, nowMs = 2_000)
        session.onFirstFrame(nowMs = 2_100)
        assertTrue(session.freezes(nowMs = 5_000).isEmpty())
    }

    @Test
    fun `finish stops the watch clock`() {
        val session = PlaybackSession()
        session.onFirstFrame(nowMs = 0)
        session.finish(nowMs = 20_000)
        assertEquals(20_000L, session.watchedMs(nowMs = 50_000))
    }
}
