package com.app.streamtest.data.device

import android.content.Context
import android.hardware.display.DisplayManager
import android.media.MediaCodecList
import android.media.MediaDrm
import android.os.Build
import android.view.Display
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import com.app.streamtest.domain.device.DecoderInfo
import com.app.streamtest.domain.device.DeviceCapabilities
import com.app.streamtest.domain.device.DeviceDetails
import com.app.streamtest.domain.device.DeviceInfoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/** Asks Android what this TV can decode and display. Read once, then cached. */
@Singleton
class AndroidDeviceInfoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceInfoRepository {

    private var cached: DeviceCapabilities? = null

    override suspend fun getCapabilities(): DeviceCapabilities {
        cached?.let { return it }
        return withContext(Dispatchers.IO) { readCapabilities() }.also { cached = it }
    }

    override suspend fun getDetails(): DeviceDetails = withContext(Dispatchers.IO) {
        val display = defaultDisplay()
        val widevine = readWidevineProperties("securityLevel", "version", "maxHdcpLevel")
        DeviceDetails(
            deviceName = deviceName(),
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            widevineLevel = widevine["securityLevel"],
            widevineVersion = widevine["version"],
            maxHdcpLevel = widevine["maxHdcpLevel"],
            videoDecoders = videoDecoders(),
            // Display.Mode = one resolution + refresh rate the screen can switch to.
            displayModes = display?.supportedModes
                ?.map { "${it.physicalWidth}×${it.physicalHeight} @ ${it.refreshRate.roundToInt()} Hz" }
                ?.distinct()
                ?: emptyList(),
            hdrTypes = (display?.let { supportedHdrTypes(it) } ?: IntArray(0)).map { hdrTypeName(it) },
        )
    }

    private fun readCapabilities(): DeviceCapabilities {
        val display = defaultDisplay()
        val hdrTypes = if (display != null) supportedHdrTypes(display) else IntArray(0)
        val decoders = videoDecoders().map { it.mimeType }.toSet()
        return DeviceCapabilities(
            deviceName = deviceName(),
            widevineLevel = readWidevineProperties("securityLevel")["securityLevel"],
            supports4k = display?.supportedModes?.any { it.physicalWidth >= 3840 && it.physicalHeight >= 2160 } ?: false,
            supportsHdr10 = Display.HdrCapabilities.HDR_TYPE_HDR10 in hdrTypes,
            supportsDolbyVision = MimeTypes.VIDEO_DOLBY_VISION in decoders &&
                Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION in hdrTypes,
            hasHevcDecoder = MimeTypes.VIDEO_H265 in decoders,
            hasAv1Decoder = MimeTypes.VIDEO_AV1 in decoders,
        )
    }

    private fun defaultDisplay(): Display? =
        context.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)

    private fun deviceName(): String = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

    // MediaDrm is Android's DRM API. Opening it for the Widevine UUID lets us ask the
    // device's Widevine module about itself, e.g. "securityLevel": "L1" = keys and
    // decoding stay inside secure hardware (most paid services require it for HD/4K),
    // "L3" = software only. Returns only the properties the device answered.
    private fun readWidevineProperties(vararg names: String): Map<String, String> {
        val mediaDrm = try {
            MediaDrm(C.WIDEVINE_UUID)
        } catch (e: Exception) {
            return emptyMap() // e.g. UnsupportedSchemeException: no Widevine on this device.
        }
        try {
            val values = mutableMapOf<String, String>()
            for (name in names) {
                try {
                    values[name] = mediaDrm.getPropertyString(name)
                } catch (e: Exception) {
                    // Not every Widevine module knows every property; skip it.
                }
            }
            return values
        } finally {
            // close() only exists from API 28; release() is the older name for the same thing.
            if (Build.VERSION.SDK_INT >= 28) mediaDrm.close() else @Suppress("DEPRECATION") mediaDrm.release()
        }
    }

    // MediaCodecList lists every hardware/software codec on the device. We keep the
    // decoders (not encoders) for video, e.g. "c2.android.hevc.decoder" for "video/hevc".
    private fun videoDecoders(): List<DecoderInfo> =
        MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
            .filter { !it.isEncoder }
            .flatMap { info ->
                info.supportedTypes
                    .filter { it.startsWith("video/", ignoreCase = true) }
                    .map { type ->
                        DecoderInfo(
                            name = info.name,
                            mimeType = type.lowercase(),
                            // Android 10 (API 29) added "is this a hardware chip?".
                            isHardware = if (Build.VERSION.SDK_INT >= 29) info.isHardwareAccelerated else null,
                        )
                    }
            }

    private fun hdrTypeName(type: Int): String = when (type) {
        Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
        Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
        Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
        Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
        else -> "HDR type $type"
    }

    // Which HDR formats the screen can show. Android 14 (API 34) moved this from
    // Display.hdrCapabilities to the current display mode.
    private fun supportedHdrTypes(display: Display): IntArray =
        if (Build.VERSION.SDK_INT >= 34) {
            display.mode.supportedHdrTypes
        } else {
            @Suppress("DEPRECATION")
            display.hdrCapabilities?.supportedHdrTypes ?: IntArray(0)
        }
}
