package com.app.streamtest.domain.device

/** What this TV can play. Read once at startup (see AGENTS.md §7). */
data class DeviceCapabilities(
    /** "L1" (hardware-protected), "L3" (software only), or null if the device has no Widevine. */
    val widevineLevel: String?,
    /** The display has a 3840×2160 (or larger) mode. */
    val supports4k: Boolean,
    /** The display can show HDR10. */
    val supportsHdr10: Boolean,
    /** There is a Dolby Vision decoder AND the display can show Dolby Vision. */
    val supportsDolbyVision: Boolean,
    /** There is a decoder for HEVC (H.265), the codec most 4K streams use. */
    val hasHevcDecoder: Boolean,
    /** There is a decoder for AV1, the newer, more efficient codec. */
    val hasAv1Decoder: Boolean,
)
