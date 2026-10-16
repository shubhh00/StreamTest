package com.app.streamtest.screens.library

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.app.streamtest.ui.theme.DimTextColor

/**
 * Remote hints on the left. On the right, a small note about the backdrop: the
 * artwork credit when one shows, or "Artwork not available" for titles without art.
 */
@Composable
fun LibraryFooter(hasArtwork: Boolean, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.bodySmall
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "▲▼ choose   ▶ versions   OK play", style = style, color = DimTextColor)
        Spacer(Modifier.weight(1f))
        Text(
            // CC BY asks for a credit; the full list is in Settings → About & credits.
            text = if (hasArtwork) "Artwork © Blender Foundation · CC BY 3.0" else "Artwork not available",
            style = style,
            color = DimTextColor,
        )
    }
}
