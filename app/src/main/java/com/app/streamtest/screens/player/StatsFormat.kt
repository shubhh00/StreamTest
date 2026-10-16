package com.app.streamtest.screens.player

import com.app.streamtest.domain.stats.StartStepKind
import com.app.streamtest.domain.stats.standardHeight
import java.util.Locale

// Small pure functions that turn numbers into the text shown on screen.

/** "1080p · H.264", or "—" before the first video format arrives. */
fun formatPlayingAt(width: Int?, height: Int?, codecs: String?, sampleMimeType: String?): String {
    if (width == null || height == null || width <= 0 || height <= 0) return "—"
    return "${standardHeight(width, height)}p · ${codecName(codecs, sampleMimeType)}"
}

/**
 * Readable codec name. The "codecs" string comes from the playlist/manifest,
 * e.g. "avc1.640028" = H.264, "hvc1.1.6.L90" = HEVC, "vp09.00..." = VP9, "av01..." = AV1.
 */
fun codecName(codecs: String?, sampleMimeType: String?): String {
    val value = (codecs ?: sampleMimeType ?: "").lowercase(Locale.ROOT)
    return when {
        value.startsWith("avc") || value.contains("video/avc") -> "H.264"
        value.startsWith("hvc") || value.startsWith("hev") || value.contains("video/hevc") -> "HEVC"
        value.startsWith("vp09") || value.startsWith("vp9") || value.contains("vp9") -> "VP9"
        value.startsWith("av01") || value.contains("video/av01") -> "AV1"
        value.startsWith("dvh") || value.contains("dolby-vision") -> "Dolby Vision"
        value.isBlank() -> "?"
        else -> value.substringAfter('/').uppercase(Locale.ROOT)
    }
}

/** "2.5 Mbps", "800 kbps", or "—" when the stream doesn't give a bitrate. */
fun formatBitrate(bitsPerSecond: Int?): String = when {
    bitsPerSecond == null -> "—"
    bitsPerSecond >= 1_000_000 -> String.format(Locale.US, "%.1f Mbps", bitsPerSecond / 1_000_000f)
    else -> "${bitsPerSecond / 1000} kbps"
}

/** "12.4 s" */
fun formatSeconds(ms: Long): String = String.format(Locale.US, "%.1f s", ms / 1000f)

/** "640 ms", or "—" when it hasn't happened. */
fun formatMs(ms: Long?): String = if (ms == null) "—" else "$ms ms"

/** "4:05" or "1:02:03". */
fun formatClock(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

fun StartStepKind.label(): String = when (this) {
    StartStepKind.MANIFEST -> "Playlist / manifest loaded"
    StartStepKind.DRM_LICENSE -> "DRM license received"
    // "Fully": the player starts decoding while a chunk is still downloading, so on
    // streams with long chunks this step often lands after the first frame.
    StartStepKind.FIRST_VIDEO_CHUNK -> "First video chunk fully downloaded"
    StartStepKind.FIRST_FRAME -> "First frame on screen"
}
