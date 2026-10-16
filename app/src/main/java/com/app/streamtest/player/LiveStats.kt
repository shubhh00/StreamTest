package com.app.streamtest.player

import com.app.streamtest.domain.stats.Freeze
import com.app.streamtest.domain.stats.QualityChange
import com.app.streamtest.domain.stats.QualityLineModel
import com.app.streamtest.domain.stats.StartMilestones

/** Everything the Player overlay shows. Updated by [StatsCollector]. */
data class LiveStats(
    // "Playing at": the video format the decoder is getting right now.
    val videoWidth: Int? = null,
    val videoHeight: Int? = null,
    val codecs: String? = null,
    val sampleMimeType: String? = null,
    /** Bits per second of the current quality, or null if the stream doesn't say. */
    val bitrate: Int? = null,
    /** How much video is downloaded ahead of the playhead. */
    val bufferedAheadMs: Long = 0,
    val milestones: StartMilestones = StartMilestones(),
    val freezeCount: Int = 0,
    val freezeTotalMs: Long = 0,
    val droppedFrames: Int = 0,
    val positionMs: Long = 0,
    /** Null for live streams and before the duration is known. */
    val durationMs: Long? = null,
    val isLive: Boolean = false,
    // Raw records for the quality line, and the line built from them.
    val qualityChanges: List<QualityChange> = emptyList(),
    val freezes: List<Freeze> = emptyList(),
    val qualityLine: QualityLineModel = QualityLineModel(),
    /** Where the playhead is on the quality line, 0..1. */
    val playheadFraction: Float = 0f,
)
