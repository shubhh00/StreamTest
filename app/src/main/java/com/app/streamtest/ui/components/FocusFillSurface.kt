package com.app.streamtest.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import com.app.streamtest.ui.theme.BackgroundColor
import com.app.streamtest.ui.theme.FocusCornerRadius
import com.app.streamtest.ui.theme.SecondaryTextColor
import com.app.streamtest.ui.theme.TextColor

// On a TV there is no touch. The user moves a "focus" between items with the D-pad
// and presses OK to click. A clickable TV Surface is focusable and changes its
// colors by itself when focus lands on it.
//
// Our focus style (AGENTS.md §9): filled off-white background, dark text,
// no glow, no border, no scaling. The content gets `focused` so it can switch
// any extra colors (like secondary text) to match.
@Composable
fun FocusFillSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unfocusedContentColor: Color = TextColor,
    content: @Composable BoxScope.(focused: Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        // onFocusChanged tells us when the D-pad focus arrives on / leaves this item.
        modifier = modifier.onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(FocusCornerRadius)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = unfocusedContentColor,
            focusedContainerColor = TextColor,
            focusedContentColor = BackgroundColor,
            pressedContainerColor = TextColor,
            pressedContentColor = BackgroundColor,
        ),
        // TV Material makes focused items grow a little by default; our design doesn't.
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        content(focused)
    }
}

/** Secondary (grey) text color that stays readable on the off-white focus fill. */
fun secondaryTextColor(focused: Boolean): Color =
    if (focused) BackgroundColor.copy(alpha = 0.65f) else SecondaryTextColor
