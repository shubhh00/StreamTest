package com.app.streamtest.domain.catalog

/** The three ways a stream can be delivered. */
enum class StreamType {
    /** Apple's streaming format. The playlist file ends in .m3u8. */
    HLS,

    /** MPEG-DASH, used by most Android streaming apps. The manifest ends in .mpd. */
    DASH,

    /** DASH where the video is encrypted with Widevine DRM. Needs a license server to play. */
    DASH_DRM,
}

/** One playable stream: one film in one format, or one extra stream. */
data class StreamVersion(
    val id: String,
    val type: StreamType,
    val url: String,
    /** Only set for [StreamType.DASH_DRM]: where the player asks for the decryption key. */
    val licenseUrl: String? = null,
    /** Short line shown under the version name, e.g. "Adaptive · up to 1080p". */
    val subtitle: String? = null,
)

/** A film that exists in up to three versions (HLS, DASH, Protected). */
data class Film(
    val id: String,
    val title: String,
    val description: String,
    val versions: List<StreamVersion>,
) {
    /** The version of this film in the given format, or null if it doesn't exist. */
    fun version(type: StreamType): StreamVersion? = versions.firstOrNull { it.type == type }
}

/** A single extra stream in the "More streams" list (4K sample, live stream, ...). */
data class ExtraStream(
    val title: String,
    val note: String,
    val stream: StreamVersion,
)

/** Everything in assets/streams.json. */
data class Catalog(
    val films: List<Film>,
    val extras: List<ExtraStream>,
)
