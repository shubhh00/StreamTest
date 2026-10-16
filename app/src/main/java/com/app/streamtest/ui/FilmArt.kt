package com.app.streamtest.ui

import androidx.annotation.DrawableRes
import com.app.streamtest.R

/**
 * Backdrop artwork bundled in res/drawable-nodpi, by film id or extra-stream id
 * (from streams.json). Extras that are one of our films reuse its art; titles
 * without official art (Angel One, the live and Apple test patterns) get none.
 *
 * All three are Creative Commons Attribution 3.0 © Blender Foundation, taken from
 * Wikimedia Commons; CC BY only asks for a credit, see [ARTWORK_CREDITS].
 */
@DrawableRes
fun backdropFor(id: String): Int? = when (id) {
    "tears", "tears-hevc" -> R.drawable.backdrop_tears
    // The AV1 sample is Big Buck Bunny too (checked by playing it).
    "bbb", "bbb-4k60", "av1-drm" -> R.drawable.backdrop_bbb
    "sintel" -> R.drawable.backdrop_sintel
    else -> null
}

/** One credit line per bundled image, shown in Settings → About & credits. */
val ARTWORK_CREDITS = listOf(
    "Tears of Steel — film still (frame 05 6a) © Blender Foundation, mango.blender.org, CC BY 3.0",
    "Big Buck Bunny — \"Frank, Rinky & Gimera\" © Blender Foundation, bigbuckbunny.org, CC BY 3.0",
    "Sintel — Durian project wallpaper © Blender Foundation, sintel.org, CC BY 3.0",
)
