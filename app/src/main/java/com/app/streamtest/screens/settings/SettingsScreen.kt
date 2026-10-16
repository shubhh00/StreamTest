package com.app.streamtest.screens.settings

import com.app.streamtest.ui.components.blockFocusExit
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.app.streamtest.ui.theme.TextColor

// Root: gets the ViewModel from Hilt and passes plain state + callbacks to the Screen.
@Composable
fun SettingsRoot(
    onOpenLibrary: () -> Unit,
    onOpenLog: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onOpenLibrary = onOpenLibrary,
        onOpenLog = onOpenLog,
        onSectionFocused = viewModel::onSectionFocused,
        onClearPressed = viewModel::onClearPressed,
    )
}

/** Sections on the left (▲▼); the focused section's details on the right, like the Library. */
@Composable
fun SettingsScreen(
    state: SettingsState,
    onOpenLibrary: () -> Unit,
    onOpenLog: () -> Unit,
    onSectionFocused: (SettingsSection) -> Unit,
    onClearPressed: () -> Unit,
) {
    // One FocusRequester per section: the first one gets focus when the screen opens,
    // and LEFT from the "Clear" button returns to "Saved results".
    val sectionFocus = remember { SettingsSection.entries.associateWith { FocusRequester() } }
    LaunchedEffect(Unit) { sectionFocus.getValue(SettingsSection.entries.first()).requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SafeAreaHorizontal, vertical = SafeAreaVertical),
    ) {
        AppHeader(
            selectedTab = HeaderTab.Settings,
            onLibraryClick = onOpenLibrary,
            onLogClick = onOpenLog,
            onSettingsClick = {},
            // DOWN from the header returns to the section you were on.
            downTarget = sectionFocus.getValue(state.section),
        )
        Spacer(Modifier.height(24.dp))
        Text(text = "Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // LEFT at the edge of the screen does nothing.
            Column(modifier = Modifier.width(240.dp).blockFocusExit(FocusDirection.Left)) {
                SettingsSection.entries.forEach { section ->
                    FocusFillSurface(
                        onClick = {}, // focus alone switches the section
                        unfocusedContentColor = if (section == state.section) TextColor else SecondaryTextColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(sectionFocus.getValue(section))
                            // RIGHT only moves into the content when there is something to press.
                            .focusProperties { if (!state.section.hasFocusableContent(state)) right = FocusRequester.Cancel }
                            .onFocusChanged { if (it.isFocused) onSectionFocused(section) },
                    ) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.width(40.dp))
            Column(modifier = Modifier.weight(1f).blockFocusExit(FocusDirection.Right, FocusDirection.Down)) {
                SectionContent(
                    state = state,
                    onClearPressed = onClearPressed,
                    resultsSectionFocus = sectionFocus.getValue(SettingsSection.RESULTS),
                )
            }
        }
    }
}

@Composable
private fun SectionContent(
    state: SettingsState,
    onClearPressed: () -> Unit,
    resultsSectionFocus: FocusRequester,
) {
    val details = state.details
    when (state.section) {
        SettingsSection.THIS_TV -> if (details != null) ThisTvSection(details) else Loading()
        SettingsSection.DECODERS -> if (details != null) DecodersSection(state.decoderGroups) else Loading()
        SettingsSection.DISPLAY -> if (details != null) DisplaySection(details) else Loading()
        SettingsSection.RESULTS -> ResultsSection(state.savedCount, state.clearStep, onClearPressed, resultsSectionFocus)
        SettingsSection.ABOUT -> AboutSection()
    }
}

@Composable
private fun Loading() {
    Text(text = "Checking this TV…", color = SecondaryTextColor)
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun SettingsScreenPreview() {
    StreamTestTheme {
        Surface {
            SettingsScreen(
                state = SettingsState(section = SettingsSection.RESULTS, savedCount = 3),
                onOpenLibrary = {},
                onOpenLog = {},
                onSectionFocused = {},
                onClearPressed = {},
            )
        }
    }
}

/** Only "Saved results" has something to press, and only while there is something to clear. */
private fun SettingsSection.hasFocusableContent(state: SettingsState): Boolean =
    this == SettingsSection.RESULTS && state.savedCount > 0 && state.clearStep != ClearStep.DONE
