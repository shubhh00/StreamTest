package com.app.streamtest.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

// We use the TV version of Material (androidx.tv.material3), not the phone one.
// Its components (Surface, Button, ListItem…) already know how to react to D-pad focus.
private val StreamTestColorScheme = darkColorScheme(
    primary = TextColor,
    onPrimary = BackgroundColor,
    background = BackgroundColor,
    onBackground = TextColor,
    surface = BackgroundColor,
    onSurface = TextColor,
    onSurfaceVariant = SecondaryTextColor,
    border = LineColor,
    error = ProblemColor,
)

@Composable
fun StreamTestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StreamTestColorScheme,
        typography = StreamTestTypography,
        content = content,
    )
}
