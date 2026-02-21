package com.etypewriter.ahc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TypewriterColorScheme = lightColorScheme(
    primary = InkBlack,
    onPrimary = Cream,
    primaryContainer = CreamDark,
    onPrimaryContainer = InkBlack,
    secondary = InkGray,
    onSecondary = Cream,
    background = Cream,
    onBackground = InkBlack,
    surface = Cream,
    onSurface = InkBlack,
    surfaceVariant = CreamDark,
    onSurfaceVariant = InkGray,
    outline = InkLightGray,
    error = RibbonRed,
    onError = Cream,
)

@Composable
fun TypewriterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TypewriterColorScheme,
        typography = TypewriterTypography,
        content = content,
    )
}
