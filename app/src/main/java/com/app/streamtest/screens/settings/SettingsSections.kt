package com.app.streamtest.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.BuildConfig
import com.app.streamtest.domain.device.DeviceDetails
import com.app.streamtest.screens.player.codecName
import com.app.streamtest.ui.ARTWORK_CREDITS
import com.app.streamtest.ui.components.FocusFillSurface
import com.app.streamtest.ui.theme.DimTextColor
import com.app.streamtest.ui.theme.ProblemColor
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.TextColor
import com.app.streamtest.ui.theme.tabularDigits

// The right-hand side of Settings: one composable per section.

@Composable
fun ThisTvSection(details: DeviceDetails) {
    Column {
        InfoRow("Device", details.deviceName)
        InfoRow("Android", details.androidVersion)
        // Color only appears on problems: no Widevine means no Protected streams.
        InfoRow("Widevine", details.widevineLevel ?: "Not available", if (details.widevineLevel == null) ProblemColor else TextColor)
        InfoRow("Widevine version", details.widevineVersion ?: "—")
        InfoRow("Max HDCP", details.maxHdcpLevel ?: "—")
        Spacer(Modifier.height(12.dp))
        Note(
            "L1 = keys and video stay in secure hardware; most paid services need it for HD/4K. " +
                "L3 = software only. HDCP protects the HDMI link to the screen.",
        )
    }
}

@Composable
fun DecodersSection(groups: List<DecoderGroup>, decodes4k: Boolean) {
    Column {
        // Decoding is separate from the screen: a 4K decoder can feed a 1080p screen.
        Text(
            text = if (decodes4k) "Can decode 4K (3840×2160)" else "Can't decode 4K (3840×2160)",
            style = MaterialTheme.typography.bodyMedium,
            color = if (decodes4k) TextColor else ProblemColor,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        groups.forEach { group ->
            val hardware = when (group.hardwareCount) {
                null -> ""
                0 -> " · software only"
                else -> " · ${group.hardwareCount} hardware"
            }
            Text(
                text = tabularDigits("${codecName(null, group.mimeType)}  ${group.mimeType}$hardware"),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = group.decoderNames.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = DimTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
fun DisplaySection(details: DeviceDetails) {
    Column {
        Label("Resolutions the screen can switch to")
        details.displayModes.forEach { mode ->
            Text(text = tabularDigits(mode), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        Label("HDR formats")
        if (details.hdrTypes.isEmpty()) {
            Text(text = "None — this screen shows standard (SDR) video only", color = ProblemColor)
        } else {
            Text(text = details.hdrTypes.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ResultsSection(
    savedCount: Int,
    clearStep: ClearStep,
    onClearPressed: () -> Unit,
    /** Where LEFT from the button goes: back to the "Saved results" section. */
    leftTarget: FocusRequester,
) {
    Column {
        Text(text = tabularDigits(if (savedCount == 1) "1 saved test" else "$savedCount saved tests"))
        Note("Clearing empties the Log and resets every Library row to \"Not tested yet\".")
        Spacer(Modifier.height(16.dp))
        when {
            clearStep == ClearStep.DONE -> Text(text = "Cleared", color = SecondaryTextColor)
            savedCount == 0 -> Text(text = "Nothing to clear", color = DimTextColor)
            else -> FocusFillSurface(
                onClick = onClearPressed,
                modifier = Modifier.focusProperties { left = leftTarget },
            ) {
                Text(
                    text = if (clearStep == ClearStep.CONFIRM) {
                        "Press OK again to delete $savedCount tests"
                    } else {
                        "Clear saved results"
                    },
                    // The confirm step is the dangerous one, so it gets the problem color.
                    color = if (clearStep == ClearStep.CONFIRM) ProblemColor else Color.Unspecified,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
fun AboutSection() {
    Column {
        InfoRow("Version", BuildConfig.VERSION_NAME)
        Note("A speed test for video on Android TV. No video is stored; only test results stay on this TV.")
        Spacer(Modifier.height(12.dp))
        Label("Test streams")
        Note("Google Media3 & Widevine samples, Shaka Player demo assets, Unified Streaming, Mux and Apple examples.")
        Spacer(Modifier.height(12.dp))
        Label("Artwork")
        ARTWORK_CREDITS.forEach { Note(it) }
        Spacer(Modifier.height(12.dp))
        Label("Font")
        Note("Schibsted Grotesk, SIL Open Font License 1.1")
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = TextColor) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = label, color = SecondaryTextColor, modifier = Modifier.width(160.dp))
        Text(text = tabularDigits(value), color = valueColor)
    }
}

@Composable
private fun Label(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = SecondaryTextColor)
}

@Composable
private fun Note(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = SecondaryTextColor)
}
