package com.app.streamtest.data.device

import android.content.Context
import android.hardware.display.DisplayManager
import android.media.MediaCodecList
import android.media.MediaDrm
import android.os.Build
import android.view.Display
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import com.app.streamtest.domain.device.DeviceCapabilities
import com.app.streamtest.domain.device.DeviceInfoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

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

    private fun readCapabilities(): DeviceCapabilities {
        val display = context.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)
        val hdrTypes = if (display != null) supportedHdrTypes(display) else IntArray(0)
        val decoders = videoDecoderMimeTypes()
        return DeviceCapabilities(
            widevineLevel = readWidevineLevel(),
            supports4k = display?.supportedModes?.any { it.physicalWidth >= 3840 && it.physicalHeight >= 2160 } ?: false,
            supportsHdr10 = Display.HdrCapabilities.HDR_TYPE_HDR10 in hdrTypes,
            supportsDolbyVision = MimeTypes.VIDEO_DOLBY_VISION in decoders &&
                Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION in hdrTypes,
            hasHevcDecoder = MimeTypes.VIDEO_H265 in decoders,
            hasAv1Decoder = MimeTypes.VIDEO_AV1 in decoders,
        )
    }

    // MediaDrm is Android's DRM API. Opening it for the Widevine UUID lets us ask the
    // device's Widevine module about itself. "L1" = keys and decoding stay inside secure
    // hardware (most paid services require it for HD/4K). "L3" = software only.
    private fun readWidevineLevel(): String? = try {
        val mediaDrm = MediaDrm(C.WIDEVINE_UUID)
        try {
            mediaDrm.getPropertyString("securityLevel")
        } finally {
            // close() only exists from API 28; release() is the older name for the same thing.
            if (Build.VERSION.SDK_INT >= 28) mediaDrm.close() else @Suppress("DEPRECATION") mediaDrm.release()
        }
    } catch (e: Exception) {
        null // e.g. UnsupportedSchemeException: this device (often an emulator) has no Widevine.
    }

    // MediaCodecList lists every hardware/software codec on the device. We keep the
    // decoders (not encoders) and collect the MIME types they accept, e.g. "video/hevc".
    private fun videoDecoderMimeTypes(): Set<String> =
        MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
            .filter { !it.isEncoder }
            .flatMap { it.supportedTypes.toList() }
            .map { it.lowercase() }
            .toSet()

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
