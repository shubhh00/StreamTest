package com.app.streamtest.screens.log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.app.streamtest.ui.components.AppHeader
import com.app.streamtest.ui.components.HeaderTab
import com.app.streamtest.ui.theme.SafeAreaHorizontal
import com.app.streamtest.ui.theme.SafeAreaVertical
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.StreamTestTheme

// Milestone 1: empty Log. The saved-tests table arrives in Milestone 6.
@Composable
fun LogScreen(onOpenLibrary: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SafeAreaHorizontal, vertical = SafeAreaVertical),
    ) {
        // This screen has nothing else focusable yet, so focus starts on the "Log" tab.
        AppHeader(
            selectedTab = HeaderTab.Log,
            onLibraryClick = onOpenLibrary,
            onLogClick = {},
            focusSelectedTabOnStart = true,
        )
        Spacer(Modifier.height(48.dp))
        Text(text = "Log", style = MaterialTheme.typography.headlineLarge)
        Text(text = "No tests saved yet.", color = SecondaryTextColor)
    }
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun LogScreenPreview() {
    StreamTestTheme {
        Surface {
            LogScreen(onOpenLibrary = {})
        }
    }
}
