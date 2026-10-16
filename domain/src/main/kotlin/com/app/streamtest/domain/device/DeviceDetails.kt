package com.app.streamtest.domain.device

/** Everything we can find out about this TV, for Settings → This TV / Decoders / Display. */
data class DeviceDetails(
    val deviceName: String,
    /** e.g. "Android 14 (API 34)". */
    val androidVersion: String,
    /** "L1" / "L3", or null when there's no Widevine. */
    val widevineLevel: String?,
    /** Version of the Widevine module, or null if unknown. */
    val widevineVersion: String?,
    /** Highest HDCP (copy protection on the HDMI link) the TV supports, or null if unknown. */
    val maxHdcpLevel: String?,
    val videoDecoders: List<DecoderInfo>,
    /** A video decoder accepts 3840×2160 frames (H.264 or HEVC). */
    val decodes4k: Boolean = false,
    /** e.g. "1920×1080 @ 60 Hz". */
    val displayModes: List<String>,
    /** e.g. "HDR10", "HLG". Empty if the screen has no HDR. */
    val hdrTypes: List<String>,
)

/** One video decoder on the device. */
data class DecoderInfo(
    /** e.g. "c2.android.hevc.decoder". */
    val name: String,
    /** e.g. "video/hevc". */
    val mimeType: String,
    /** True = a hardware chip, false = software. Null on Android 9 and older (not reported). */
    val isHardware: Boolean?,
)
