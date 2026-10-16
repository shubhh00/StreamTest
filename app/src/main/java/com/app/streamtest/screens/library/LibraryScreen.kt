package com.app.streamtest.screens.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.app.streamtest.ui.components.AppHeader
import com.app.streamtest.ui.components.FocusFillSurface
import com.app.streamtest.ui.components.HeaderTab
import com.app.streamtest.ui.theme.SafeAreaHorizontal
import com.app.streamtest.ui.theme.SafeAreaVertical
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.StreamTestTheme

// Milestone 1: an empty Library with one test row, to prove navigation and focus work.
// Films, versions and the device footer arrive in Milestone 2.
@Composable
fun LibraryScreen(
    onOpenPlayer: (streamId: String) -> Unit,
    onOpenLog: () -> Unit,
) {
    // When a screen opens, nothing is focused until the user presses a key.
    // We move focus onto the first row ourselves so the D-pad works right away.
    val firstRowFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstRowFocus.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SafeAreaHorizontal, vertical = SafeAreaVertical),
    ) {
        AppHeader(selectedTab = HeaderTab.Library, onLibraryClick = {}, onLogClick = onOpenLog)
        Spacer(Modifier.height(48.dp))
        Text(text = "Library", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Films and their HLS / DASH / Protected versions arrive in Milestone 2.",
            color = SecondaryTextColor,
        )
        Spacer(Modifier.height(24.dp))
        FocusFillSurface(
            onClick = { onOpenPlayer("test-stream") },
            modifier = Modifier.focusRequester(firstRowFocus),
        ) {
            Text(
                text = "Open the player (test route)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun LibraryScreenPreview() {
    StreamTestTheme {
        Surface {
            LibraryScreen(onOpenPlayer = {}, onOpenLog = {})
        }
    }
}
