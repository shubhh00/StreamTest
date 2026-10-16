package com.app.streamtest.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi

/** One video quality the stream offers, as shown in the Quality panel. */
data class QualityOption(
    /** "groupIndex:trackIndex" — enough to find this track again in player.currentTracks. */
    val key: String,
    val width: Int,
    val height: Int,
    /** Bits per second, or null if the stream doesn't say. */
    val bitrate: Int?,
)

/**
 * The video qualities in the player's current tracks, sharpest first.
 *
 * player.currentTracks lists every track the stream offers, in "groups". An adaptive
 * stream has one video group holding the same video at several qualities; the player
 * is allowed to switch between the tracks of a group.
 */
fun Tracks.videoQualityOptions(): List<QualityOption> {
    val options = mutableListOf<QualityOption>()
    groups.forEachIndexed { groupIndex, group ->
        if (group.type != C.TRACK_TYPE_VIDEO) return@forEachIndexed
        for (trackIndex in 0 until group.length) {
            val format = group.getTrackFormat(trackIndex)
            // Skip tracks this device can't decode, and tracks without a picture size.
            if (!group.isTrackSupported(trackIndex) || format.height <= 0) continue
            options.add(
                QualityOption(
                    key = "$groupIndex:$trackIndex",
                    width = format.width,
                    height = format.height,
                    bitrate = format.bitrateOrNull(),
                ),
            )
        }
    }
    return options.sortedWith(compareByDescending<QualityOption> { it.height }.thenByDescending { it.bitrate ?: 0 })
}

/**
 * The track's bitrate in bits per second, or null if the stream doesn't say.
 * Playlists give a peak (highest) and/or an average bitrate; we prefer the peak,
 * which is what HLS playlists list as BANDWIDTH. Format.NO_VALUE (-1) = not given.
 *
 * Media3 marks all of Format's bitrate fields as @UnstableApi (they may change between
 * versions), so we opt in here, in this one small function.
 */
@OptIn(UnstableApi::class)
fun Format.bitrateOrNull(): Int? = when {
    peakBitrate != Format.NO_VALUE -> peakBitrate
    averageBitrate != Format.NO_VALUE -> averageBitrate
    else -> null
}

/**
 * A TrackSelectionOverride says "for this group, use exactly these tracks", which
 * switches off the automatic choice for video. Null if the key no longer exists.
 */
fun Tracks.overrideFor(key: String): TrackSelectionOverride? {
    val (groupIndex, trackIndex) = key.split(":").map { it.toInt() }
    val group = groups.getOrNull(groupIndex) ?: return null
    if (trackIndex >= group.length) return null
    return TrackSelectionOverride(group.mediaTrackGroup, trackIndex)
}
