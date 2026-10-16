package com.app.streamtest.domain.device

/**
 * What this TV can play. Read once at startup (see AGENTS.md §7).
 *
 * Decoding and displaying are kept apart on purpose: a TV box can decode 4K and send
 * it to a 1080p screen (it gets scaled down), so "decodes 4K" and "4K screen" are
 * different questions.
 */
data class DeviceCapabilities(
    /** e.g. "Google Chromecast" or the emulator's model name. Shown in the Log. */
    val deviceName: String = "",
    /** "L1" (hardware-protected), "L3" (software only), or null if the device has no Widevine. */
    val widevineLevel: String?,
    /** A video decoder accepts 3840×2160 frames (H.264 or HEVC). */
    val decodes4k: Boolean,
    /** There is a decoder for HEVC (H.265), the codec most 4K streams use. */
    val hasHevcDecoder: Boolean,
    /** There is a decoder for AV1, the newer, more efficient codec. */
    val hasAv1Decoder: Boolean,
    /** There is a Dolby Vision decoder. */
    val hasDolbyVisionDecoder: Boolean,
    /** Short side of the screen's largest mode (1080, 2160…), or 0 if unknown. */
    val screenMaxHeight: Int = 0,
    /** HDR formats the screen can show, e.g. "HDR10", "HLG". Empty = SDR only. */
    val screenHdrTypes: List<String> = emptyList(),
)
