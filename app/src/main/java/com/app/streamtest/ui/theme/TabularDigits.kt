package com.app.streamtest.ui.theme

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Gives the digits in [text] equal widths ("tnum"), so numbers that change all the
 * time don't make the text jiggle, while "." ":" and letters keep their normal widths.
 *
 * Why not put "tnum" on the whole TextStyle? In Schibsted Grotesk the tabular feature
 * also widens punctuation, so "1.3 Mbps" looked like "1 . 3 Mbps".
 */
fun tabularDigits(text: String): AnnotatedString = buildAnnotatedString {
    var start = 0
    while (start < text.length) {
        // Find the end of the current run of digits (or of non-digits).
        val isDigitRun = text[start].isDigit()
        var end = start
        while (end < text.length && text[end].isDigit() == isDigitRun) end++

        if (isDigitRun) {
            withStyle(SpanStyle(fontFeatureSettings = TabularNumbers)) { append(text.substring(start, end)) }
        } else {
            append(text.substring(start, end))
        }
        start = end
    }
}
