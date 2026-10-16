package com.app.streamtest.screens.log

import com.app.streamtest.ui.components.blockFocusExit
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.app.streamtest.domain.stats.ResultsSummary
import com.app.streamtest.ui.components.AppHeader
import com.app.streamtest.ui.components.HeaderTab
import com.app.streamtest.ui.theme.LineColor
import com.app.streamtest.ui.theme.SafeAreaHorizontal
import com.app.streamtest.ui.theme.SafeAreaVertical
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.StreamTestTheme
import com.app.streamtest.ui.theme.tabularDigits
import java.util.Locale

// Root: gets the ViewModel from Hilt and passes plain state to the Screen.
@Composable
fun LogRoot(
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: LogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LogScreen(state = state, onOpenLibrary = onOpenLibrary, onOpenSettings = onOpenSettings)
}

/** Every saved test, grouped by film, with a one-line summary on top. */
@Composable
fun LogScreen(state: LogState, onOpenLibrary: () -> Unit, onOpenSettings: () -> Unit) {
    // Focus starts on the first row once there are results (▲▼ then moves through them).
    // With no results, the "Log" tab in the header takes focus instead.
    val firstRowFocus = remember { FocusRequester() }
    val hasRows = state.rows.isNotEmpty()
    LaunchedEffect(hasRows) {
        if (hasRows) firstRowFocus.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SafeAreaHorizontal, vertical = SafeAreaVertical),
    ) {
        AppHeader(
            selectedTab = HeaderTab.Log,
            onLibraryClick = onOpenLibrary,
            onLogClick = {},
            onSettingsClick = onOpenSettings,
            focusSelectedTabOnStart = !hasRows,
        )
        Spacer(Modifier.height(24.dp))
        Text(text = "Log", style = MaterialTheme.typography.headlineLarge)
        Text(text = subtitle(state), color = SecondaryTextColor)
        Spacer(Modifier.height(12.dp))

        val summary = state.summary
        if (!hasRows || summary == null) {
            Text(
                text = "No tests saved yet. Play a stream from the Library, then press BACK to save it.",
                color = SecondaryTextColor,
            )
            return@Column
        }

        Text(
            text = tabularDigits(summaryLine(summary)),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(16.dp))
        LogTableHeader()
        Box(Modifier.fillMaxWidth().height(1.dp).background(LineColor))
        // LazyColumn only builds the rows that are on screen, and scrolls by itself
        // when D-pad focus moves to a row below the edge.
        // LEFT / RIGHT do nothing on a row: there's nothing beside it.
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .blockFocusExit(FocusDirection.Left, FocusDirection.Right),
        ) {
            itemsIndexed(state.rows, key = { _, row -> row.result.id }) { index, row ->
                LogTableRow(
                    row = row,
                    modifier = if (index == 0) Modifier.focusRequester(firstRowFocus) else Modifier,
                )
            }
        }
    }
}

private fun subtitle(state: LogState): String {
    val tests = if (state.testCount == 1) "1 test" else "${state.testCount} tests"
    return if (state.deviceName.isBlank()) tests else "${state.deviceName} · $tests"
}

/** "Median time to start 1640 ms · 0.4 freezes per test · 82% of time in HD or better" */
private fun summaryLine(summary: ResultsSummary): String {
    val median = summary.medianTimeToStartMs?.let { "$it ms" } ?: "—"
    val freezes = String.format(Locale.US, "%.1f", summary.freezesPerTest)
    return "Median time to start $median  ·  $freezes freezes per test  ·  " +
        "${summary.hdOrBetterPercent}% of time in HD or better"
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun LogScreenEmptyPreview() {
    StreamTestTheme {
        Surface {
            LogScreen(state = LogState(deviceName = "Google TV Streamer"), onOpenLibrary = {}, onOpenSettings = {})
        }
    }
}
