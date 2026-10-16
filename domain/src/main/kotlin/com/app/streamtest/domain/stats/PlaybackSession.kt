package com.app.streamtest.domain.stats

/**
 * The watch clock and freeze bookkeeping for one test, as plain logic.
 *
 * Every function takes `nowMs` (from a clock that never jumps) instead of reading a
 * clock itself, so unit tests can play out "watch 10 s, press HOME, come back,
 * freeze, exit" step by step.
 *
 * Two rules:
 * - **Watched time** runs only after the first frame and while the user wants it to
 *   play. Paused or in the background (HOME) doesn't count; freezes do, because the
 *   viewer is sitting there waiting.
 * - A **freeze** is buffering after the first frame while the user wants to play.
 *   It ends when buffering ends, when the user pauses, or when the test finishes.
 */
class PlaybackSession {

    var firstFrameShown = false
        private set

    private var wantsToPlay = true
    private var isBuffering = false

    private var watchedBeforeMs = 0L
    private var watchingSinceMs: Long? = null

    private var openFreezeStartMs: Long? = null
    private var openFreezePositionMs = 0L
    private val closedFreezes = mutableListOf<Freeze>()

    fun onFirstFrame(nowMs: Long) {
        if (firstFrameShown) return
        firstFrameShown = true
        updateClock(nowMs)
    }

    /** playWhenReady changed: the user paused, or the app went to / came back from the background. */
    fun onWantsToPlayChanged(wantsToPlay: Boolean, positionMs: Long, nowMs: Long) {
        this.wantsToPlay = wantsToPlay
        updateClock(nowMs)
        updateFreeze(positionMs, nowMs)
    }

    /** The player started or stopped buffering. */
    fun onBufferingChanged(isBuffering: Boolean, positionMs: Long, nowMs: Long) {
        this.isBuffering = isBuffering
        updateFreeze(positionMs, nowMs)
    }

    /** Time the user spent watching (see the class comment). */
    fun watchedMs(nowMs: Long): Long = watchedBeforeMs + (watchingSinceMs?.let { nowMs - it } ?: 0L)

    /** All freezes so far; one still going is included, measured up to [nowMs]. */
    fun freezes(nowMs: Long): List<Freeze> {
        val openStart = openFreezeStartMs ?: return closedFreezes.toList()
        return closedFreezes + Freeze(positionMs = openFreezePositionMs, durationMs = nowMs - openStart)
    }

    /** The test is over: close a freeze that's still going and stop the watch clock. */
    fun finish(nowMs: Long) {
        closeFreeze(nowMs)
        stopClock(nowMs)
        wantsToPlay = false
    }

    private fun updateClock(nowMs: Long) {
        val shouldRun = firstFrameShown && wantsToPlay
        if (shouldRun && watchingSinceMs == null) watchingSinceMs = nowMs
        if (!shouldRun) stopClock(nowMs)
    }

    private fun stopClock(nowMs: Long) {
        val since = watchingSinceMs ?: return
        watchedBeforeMs += nowMs - since
        watchingSinceMs = null
    }

    private fun updateFreeze(positionMs: Long, nowMs: Long) {
        val frozen = firstFrameShown && wantsToPlay && isBuffering
        if (frozen && openFreezeStartMs == null) {
            openFreezeStartMs = nowMs
            openFreezePositionMs = positionMs
        }
        if (!frozen) closeFreeze(nowMs)
    }

    private fun closeFreeze(nowMs: Long) {
        val start = openFreezeStartMs ?: return
        closedFreezes.add(Freeze(positionMs = openFreezePositionMs, durationMs = nowMs - start))
        openFreezeStartMs = null
    }
}
