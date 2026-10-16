package com.app.streamtest.domain.device

/** The Library footer: "Plays: …" and "Can't play: …". */
data class DeviceSummary(
    val canPlay: List<String>,
    val cantPlay: List<String>,
)

/** Sorts each capability into the "plays" or the "can't play" list, in a fixed order. */
fun DeviceCapabilities.toSummary(): DeviceSummary {
    val canPlay = mutableListOf<String>()
    val cantPlay = mutableListOf<String>()

    fun add(label: String, supported: Boolean) {
        if (supported) canPlay.add(label) else cantPlay.add(label)
    }

    add(if (widevineLevel != null) "Widevine $widevineLevel" else "Widevine", widevineLevel != null)
    add("4K", supports4k)
    add("HDR10", supportsHdr10)
    add("HEVC", hasHevcDecoder)
    add("AV1", hasAv1Decoder)
    add("Dolby Vision", supportsDolbyVision)

    return DeviceSummary(canPlay = canPlay, cantPlay = cantPlay)
}
