package com.dayxday.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BluePrimary,
    secondary = GreenAccent,
    onSecondary = androidx.compose.ui.graphics.Color.White
)

@Composable
fun DayXDayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
