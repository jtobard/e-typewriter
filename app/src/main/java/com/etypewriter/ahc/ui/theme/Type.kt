package com.etypewriter.ahc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TypewriterFontFamily = FontFamily.Monospace

val TypewriterTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = TypewriterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp,
        color = InkBlack,
    ),
    bodyMedium = TextStyle(
        fontFamily = TypewriterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp,
        color = InkBlack,
    ),
    titleLarge = TextStyle(
        fontFamily = TypewriterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = InkBlack,
    ),
    titleMedium = TextStyle(
        fontFamily = TypewriterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = InkBlack,
    ),
    labelSmall = TextStyle(
        fontFamily = TypewriterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = InkLightGray,
    ),
)
