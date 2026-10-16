package com.app.streamtest.player

import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.drm.KeyRequestInfo
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import com.app.streamtest.domain.stats.PlaybackSession
import com.app.streamtest.domain.stats.QualityChange
import com.app.streamtest.domain.stats.buildQualityLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Listens to the player and turns its events into [LiveStats].
 *
 * An AnalyticsListener is Media3's "tell me everything" listener: every load,
 * format change, dropped frame and DRM event is reported to it, on the main thread.
 *
 * The watch clock and freezes are kept by [PlaybackSession] (plain, unit-tested logic);
 * this class only feeds it the player's events with the current time. One source for
 * the freeze count, total and dots means they always agree, also when the user leaves
 * in the middle of a freeze.
 *
 * Media3 marks AnalyticsListener as @UnstableApi (its shape may change between
 * versions), so we opt in on purpose.
 */
@OptIn(UnstableApi::class)
class StatsCollector(private val player: ExoPlayer) : AnalyticsListener {

    private val session = PlaybackSession()

    private val _stats = MutableStateFlow(LiveStats())
    val stats: StateFlow<LiveStats> = _stats.asStateFlow()

    // All timings use elapsedRealtime: a clock that never jumps (unlike the wall clock).
    private var prepareRealtimeMs = 0L

    fun attach() {
        player.addAnalyticsListener(this)
    }

    /** Call right before player.prepare(): all start-up times are measured from here. */
    fun onPrepare() {
        prepareRealtimeMs = SystemClock.elapsedRealtime()
    }

    private fun now(): Long = SystemClock.elapsedRealtime()

    private fun msSincePrepare(): Long = now() - prepareRealtimeMs

    // A load finished: either the playlist/manifest, or a chunk of media.
    override fun onLoadCompleted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        val milestones = _stats.value.milestones
        if (mediaLoadData.dataType == C.DATA_TYPE_MANIFEST && milestones.manifestLoadedMs == null) {
            _stats.update { it.copy(milestones = it.milestones.copy(manifestLoadedMs = msSincePrepare())) }
        }
        // HLS chunks often hold video and audio together ("muxed"); Media3 reports those
        // as TRACK_TYPE_DEFAULT. Both count as a video chunk.
        val isVideoChunk = mediaLoadData.dataType == C.DATA_TYPE_MEDIA &&
            (mediaLoadData.trackType == C.TRACK_TYPE_VIDEO || mediaLoadData.trackType == C.TRACK_TYPE_DEFAULT)
        if (isVideoChunk && milestones.firstVideoChunkMs == null) {
            _stats.update { it.copy(milestones = it.milestones.copy(firstVideoChunkMs = msSincePrepare())) }
        }
    }

    // The license server answered and the decryption keys are ready (protected streams only).
    override fun onDrmKeysLoaded(eventTime: EventTime, keyRequestInfo: KeyRequestInfo) {
        if (_stats.value.milestones.drmKeysLoadedMs == null) {
            _stats.update { it.copy(milestones = it.milestones.copy(drmKeysLoadedMs = msSincePrepare())) }
        }
    }

    override fun onRenderedFirstFrame(eventTime: EventTime, output: Any, renderTimeMs: Long) {
        if (session.firstFrameShown) return
        session.onFirstFrame(now())
        _stats.update { it.copy(milestones = it.milestones.copy(firstFrameMs = msSincePrepare())) }
    }

    // The decoder got a new video format: the start, or an adaptive quality switch.
    override fun onVideoInputFormatChanged(
        eventTime: EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?,
    ) {
        val change = QualityChange(positionMs = timelinePositionMs(), width = format.width, height = format.height)
        _stats.update {
            it.copy(
                videoWidth = format.width,
                videoHeight = format.height,
                codecs = format.codecs,
                sampleMimeType = format.sampleMimeType,
                bitrate = format.bitrateOrNull(),
                qualityChanges = it.qualityChanges + change,
            )
        }
    }

    // Frames the decoder couldn't show in time (the device was too slow).
    override fun onDroppedVideoFrames(eventTime: EventTime, droppedFrames: Int, elapsedMs: Long) {
        _stats.update { it.copy(droppedFrames = it.droppedFrames + droppedFrames) }
    }

    // Buffering started or stopped. After the first frame, while the user wants to play,
    // that's a freeze; PlaybackSession decides and measures it.
    override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
        session.onBufferingChanged(state == Player.STATE_BUFFERING, timelinePositionMs(), now())
    }

    // playWhenReady changed: the user (or HOME, via the ViewModel) paused or resumed.
    // Paused / background time doesn't count as watched time, and ends a freeze.
    override fun onPlayWhenReadyChanged(eventTime: EventTime, playWhenReady: Boolean, reason: Int) {
        session.onWantsToPlayChanged(playWhenReady, timelinePositionMs(), now())
    }

    /** Time actually watched so far (paused / background time left out). */
    fun watchedMs(): Long = session.watchedMs(now())

    /**
     * The test is over (BACK): close a freeze that's still going and stop the clock, then
     * refresh the numbers one last time, so the saved result is complete.
     */
    fun finish() {
        session.finish(now())
        onTick()
    }

    /** Called every 500 ms by the ViewModel for values that change all the time. */
    fun onTick() {
        val freezes = session.freezes(now()) // includes a freeze still going, measured up to now
        val isLive = player.isCurrentMediaItemLive
        val positionMs = timelinePositionMs()
        // A live stream has no end, so its line covers "time watched so far".
        val durationMs = if (isLive || player.duration == C.TIME_UNSET) null else player.duration
        val lineLengthMs = durationMs ?: positionMs

        _stats.update {
            it.copy(
                bufferedAheadMs = player.totalBufferedDuration,
                freezes = freezes,
                freezeCount = freezes.size,
                freezeTotalMs = freezes.sumOf { freeze -> freeze.durationMs },
                positionMs = positionMs,
                durationMs = durationMs,
                isLive = isLive,
                qualityLine = buildQualityLine(it.qualityChanges, freezes, positionMs, lineLengthMs),
                playheadFraction = if (lineLengthMs > 0) (positionMs.toFloat() / lineLengthMs).coerceIn(0f, 1f) else 0f,
            )
        }
    }

    // Where we are on the quality line. For normal videos that's the playback position.
    // A live stream's position is inside a moving window, so we use time watched instead
    // (which leaves out paused and background time).
    private fun timelinePositionMs(): Long =
        if (player.isCurrentMediaItemLive) session.watchedMs(now()) else player.currentPosition
}
