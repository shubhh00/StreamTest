package com.app.streamtest.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Typography
import com.app.streamtest.R

// res/font/schibsted_grotesk.ttf is a "variable font": one file that contains every
// weight. Each Font() entry below picks a weight from it with FontVariation.weight().
// (Variable fonts need API 26+, which is our minSdk.)
val SchibstedGrotesk = FontFamily(
    Font(
        R.font.schibsted_grotesk,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.schibsted_grotesk,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.schibsted_grotesk,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
)

// "tnum" = tabular numbers: every digit has the same width, so changing stats
// don't make the text jump around. Use for all numbers on screen.
const val TabularNumbers = "tnum"

// Film titles in the Library's left column. Regular weight, not bold.
// The spec asks for 54sp, but a 1080p TV is only 960dp wide and "Big Buck Bunny"
// didn't fit in half the screen at 54sp, so we use 48sp.
val FilmTitleStyle = TextStyle(
    fontFamily = SchibstedGrotesk,
    fontWeight = FontWeight.Normal,
    fontSize = 48.sp,
    lineHeight = 58.sp,
)

private val default = Typography()

private fun TextStyle.withAppFont() = copy(fontFamily = SchibstedGrotesk)

// The default TV Material type scale, switched to Schibsted Grotesk.
val StreamTestTypography = Typography(
    displayLarge = default.displayLarge.withAppFont(),
    displayMedium = default.displayMedium.withAppFont(),
    displaySmall = default.displaySmall.withAppFont(),
    headlineLarge = default.headlineLarge.withAppFont(),
    headlineMedium = default.headlineMedium.withAppFont(),
    headlineSmall = default.headlineSmall.withAppFont(),
    titleLarge = default.titleLarge.withAppFont(),
    titleMedium = default.titleMedium.withAppFont(),
    titleSmall = default.titleSmall.withAppFont(),
    bodyLarge = default.bodyLarge.withAppFont(),
    bodyMedium = default.bodyMedium.withAppFont(),
    bodySmall = default.bodySmall.withAppFont(),
    labelLarge = default.labelLarge.withAppFont(),
    labelMedium = default.labelMedium.withAppFont(),
    labelSmall = default.labelSmall.withAppFont(),
)
