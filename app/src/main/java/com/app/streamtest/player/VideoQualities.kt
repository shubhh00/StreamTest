package com.app.streamtest.player

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks

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
                    bitrate = format.bitrate.takeIf { it != Format.NO_VALUE },
                ),
            )
        }
    }
    return options.sortedWith(compareByDescending<QualityOption> { it.height }.thenByDescending { it.bitrate ?: 0 })
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
