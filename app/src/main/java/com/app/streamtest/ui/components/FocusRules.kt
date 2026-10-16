package com.app.streamtest.ui.components

import androidx.compose.foundation.focusGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusProperties

/**
 * Keeps D-pad focus inside this area when the user presses [direction] (or
 * [otherDirection]). Without it, pressing RIGHT on the last thing on the right makes Compose
 * pick "the nearest focusable item" anywhere on screen, often the header tabs, which
 * feels like focus jumped at random.
 *
 * focusGroup() makes the area one unit for focus; onExit runs when focus is about to
 * leave it, and cancelFocusChange() keeps it where it is.
 */
fun Modifier.blockFocusExit(direction: FocusDirection, otherDirection: FocusDirection? = null): Modifier =
    this
        .focusProperties {
            onExit = {
                if (requestedFocusDirection == direction || requestedFocusDirection == otherDirection) {
                    cancelFocusChange()
                }
            }
        }
        .focusGroup()
