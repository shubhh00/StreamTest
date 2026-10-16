package com.app.streamtest.screens.library

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.app.streamtest.ui.theme.BackgroundColor

/**
 * The focused film's artwork, full-bleed behind the Library, darkened so text stays
 * readable. Crossfade animates from the old image to the new one when the focused
 * film changes (and fades out for films and extras without art).
 */
@Composable
fun FilmBackdrop(@DrawableRes imageRes: Int?, modifier: Modifier = Modifier) {
    Crossfade(
        targetState = imageRes,
        animationSpec = tween(durationMillis = 600),
        label = "backdrop",
        modifier = modifier,
    ) { res ->
        if (res == null) return@Crossfade
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(res),
                contentDescription = null, // decoration only
                contentScale = ContentScale.Crop,
                // Keep the right side of the picture: the text sits on the left.
                alignment = Alignment.CenterEnd,
                // graphicsLayer alpha fades the image without recomposing it.
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.6f },
            )
            // Darkening layers ("scrims"). The spec avoids gradients in general; here they
            // are what keeps the film titles and rows readable on top of a photo.
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        0f to BackgroundColor,
                        0.45f to BackgroundColor.copy(alpha = 0.8f),
                        1f to BackgroundColor.copy(alpha = 0.45f),
                    ),
                ),
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0.6f to Color.Transparent, 1f to BackgroundColor),
                ),
            )
        }
    }
}
