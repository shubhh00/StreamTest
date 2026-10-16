package com.app.streamtest.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.ui.theme.DimTextColor
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.TextColor

enum class HeaderTab { Library, Log }

// "streamtest" wordmark on the left, tabs on the right.
@Composable
fun AppHeader(
    selectedTab: HeaderTab,
    onLibraryClick: () -> Unit,
    onLogClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusSelectedTabOnStart: Boolean = false,
) {
    // A FocusRequester is a handle we attach to one item so code can move the
    // D-pad focus onto it (focusRequester.requestFocus()).
    val selectedTabFocus = remember { FocusRequester() }
    if (focusSelectedTabOnStart) {
        LaunchedEffect(Unit) { selectedTabFocus.requestFocus() }
    }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "streamtest",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.weight(1f))
        HeaderTabItem(
            label = "Library",
            selected = selectedTab == HeaderTab.Library,
            onClick = onLibraryClick,
            focusRequester = selectedTabFocus.takeIf { selectedTab == HeaderTab.Library },
        )
        Spacer(Modifier.width(8.dp))
        HeaderTabItem(
            label = "Log",
            selected = selectedTab == HeaderTab.Log,
            onClick = onLogClick,
            focusRequester = selectedTabFocus.takeIf { selectedTab == HeaderTab.Log },
        )
        Spacer(Modifier.width(8.dp))
        // Settings is an empty placeholder in v1: shown, but not focusable.
        Text(
            text = "Settings",
            color = DimTextColor,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun HeaderTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    focusRequester: FocusRequester?,
) {
    val focusModifier = if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
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
}
