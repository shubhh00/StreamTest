package com.app.streamtest.ui.components

import androidx.compose.ui.focus.focusProperties
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.TextColor

enum class HeaderTab { Library, Log, Settings }

// "streamtest" wordmark on the left, tabs on the right.
@Composable
fun AppHeader(
    selectedTab: HeaderTab,
    onLibraryClick: () -> Unit,
    onLogClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusSelectedTabOnStart: Boolean = false,
    /**
     * Where DOWN from any tab goes: the screen's selected item, so leaving the header
     * returns you to where you were. Default = the nearest item below.
     */
    downTarget: FocusRequester = FocusRequester.Default,
) {
    // A FocusRequester is a handle we attach to one item so code can move the
    // D-pad focus onto it (focusRequester.requestFocus()).
    val selectedTabFocus = remember { FocusRequester() }
    if (focusSelectedTabOnStart) {
        LaunchedEffect(Unit) { selectedTabFocus.requestFocus() }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            // Coming UP into the header lands on the current screen's tab (not just the
            // nearest one), and LEFT / RIGHT past the first / last tab do nothing.
            .focusProperties { onEnter = { selectedTabFocus.requestFocus() } }
            .blockFocusExit(FocusDirection.Left, FocusDirection.Right),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "streamtest",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.weight(1f))
        HeaderTab.entries.forEachIndexed { index, tab ->
            if (index > 0) Spacer(Modifier.width(8.dp))
            HeaderTabItem(
                label = tab.name,
                selected = tab == selectedTab,
                onClick = when (tab) {
                    HeaderTab.Library -> onLibraryClick
                    HeaderTab.Log -> onLogClick
                    HeaderTab.Settings -> onSettingsClick
                },
                focusRequester = selectedTabFocus.takeIf { tab == selectedTab },
                downTarget = downTarget,
            )
        }
    }
}

@Composable
private fun HeaderTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    focusRequester: FocusRequester?,
    downTarget: FocusRequester,
) {
    val focusModifier = (if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
        .focusProperties { down = downTarget }
    // The tab of the current screen keeps a short underline, whatever has focus, so
    // "where am I" and "where is the focus" can't be confused on any screen.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FocusFillSurface(
            onClick = onClick,
            modifier = focusModifier,
            unfocusedContentColor = if (selected) TextColor else SecondaryTextColor,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(if (selected) TextColor else Color.Transparent),
        )
    }
}
