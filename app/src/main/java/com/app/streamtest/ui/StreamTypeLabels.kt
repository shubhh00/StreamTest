package com.app.streamtest.ui

import com.app.streamtest.domain.catalog.StreamType

/** The name shown for each version row. */
fun StreamType.label(): String = when (this) {
    StreamType.HLS -> "HLS"
    StreamType.DASH -> "DASH"
    StreamType.DASH_DRM -> "Protected"
}

/** Used when streams.json gives no subtitle for a version. */
fun StreamType.defaultSubtitle(): String = when (this) {
    StreamType.HLS -> "Apple HLS · .m3u8 playlist"
    StreamType.DASH -> "MPEG-DASH · .mpd manifest"
    StreamType.DASH_DRM -> "DASH with Widevine DRM"
}

/** Shown in the result column until Milestone 6 saves real results. */
const val NOT_TESTED_YET = "Not tested yet"
