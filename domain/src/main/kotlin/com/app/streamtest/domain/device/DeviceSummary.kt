package com.app.streamtest.domain.device

/** The Library's capability strip. */
data class DeviceSummary(
    /** What the device can decode / decrypt, e.g. "Widevine L1", "4K", "HEVC". */
    val canPlay: List<String>,
    val cantPlay: List<String>,
    /** The screen on its own, e.g. "1080p screen · SDR" or "2160p screen · HDR10, HLG". */
    val screen: String,
)

/** Sorts each decoder / DRM capability into "plays" or "can't play", and describes the screen. */
fun DeviceCapabilities.toSummary(): DeviceSummary {
    val canPlay = mutableListOf<String>()
    val cantPlay = mutableListOf<String>()

    fun add(label: String, supported: Boolean) {
        if (supported) canPlay.add(label) else cantPlay.add(label)
    }

    add(if (widevineLevel != null) "Widevine $widevineLevel" else "Widevine", widevineLevel != null)
    add("4K", decodes4k)
    add("HEVC", hasHevcDecoder)
    add("AV1", hasAv1Decoder)
    add("Dolby Vision", hasDolbyVisionDecoder)

    val resolution = if (screenMaxHeight > 0) "${screenMaxHeight}p screen" else "Screen"
    val hdr = if (screenHdrTypes.isEmpty()) "SDR" else screenHdrTypes.joinToString(", ")
    return DeviceSummary(canPlay = canPlay, cantPlay = cantPlay, screen = "$resolution · $hdr")
}
