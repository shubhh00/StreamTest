package com.app.streamtest.ui.theme

import androidx.compose.ui.graphics.Color

// Design tokens from AGENTS.md §9. Color only appears on problems.
val BackgroundColor = Color(0xFF121211)
val TextColor = Color(0xFFF2F0EB)
val SecondaryTextColor = Color(0xFF8A8780)
val DimTextColor = Color(0xFF45433F)
val LineColor = Color(0xFF2A2A28)
// Focused Log row: a subtle lift, darker than LineColor so the quality line's track still shows.
val RowFocusColor = Color(0xFF1E1E1C)
val ProblemColor = Color(0xFFE5674A)

// Quality line shades: brighter = sharper picture.
val QualityLowColor = Color(0xFF5C5A55)   // 480p and below
val QualityMidColor = Color(0xFF8A8780)   // 720p
val QualityHighColor = Color(0xFFF2F0EB)  // 1080p and above
