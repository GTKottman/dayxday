package com.dayxday.app.ui.theme

import androidx.compose.ui.graphics.Color

val BluePrimary = Color(0xFF2563EB)
val BlueContainer = Color(0xFFDBEAFE)
val GreenAccent = Color(0xFF059669)
val OrangeAccent = Color(0xFFEA580C)
val PurpleAccent = Color(0xFF7C3AED)
val PinkAccent = Color(0xFFDB2777)

val PresetColors = listOf(
    BluePrimary,
    GreenAccent,
    OrangeAccent,
    PurpleAccent,
    PinkAccent,
    Color(0xFF0891B2),
    Color(0xFFCA8A04),
    Color(0xFFDC2626)
)

fun Color.toArgbLong(): Long {
    val argb = (alpha * 255).toInt() shl 24 or
        (red * 255).toInt() shl 16 or
        (green * 255).toInt() shl 8 or
        (blue * 255).toInt()
    return argb.toLong() and 0xFFFFFFFFL
}

fun Long.toComposeColor(): Color = Color(this.toInt())
