package io.github.codingfulalt.cairn.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private fun style(
    size: Int,
    line: Int,
    weight: FontWeight,
    tracking: TextUnit = 0.sp,
) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking,
)

internal val CairnTypography =
    Typography(
        displayLarge = style(56, 60, FontWeight.ExtraBold, (-2).sp),
        displayMedium = style(44, 48, FontWeight.ExtraBold, (-1.5).sp),
        displaySmall = style(36, 40, FontWeight.Bold, (-1).sp),
        headlineLarge = style(32, 38, FontWeight.Bold, (-1).sp),
        headlineMedium = style(28, 34, FontWeight.Bold, (-0.6).sp),
        headlineSmall = style(24, 30, FontWeight.Bold, (-0.4).sp),
        titleLarge = style(20, 26, FontWeight.SemiBold, (-0.2).sp),
        titleMedium = style(16, 22, FontWeight.SemiBold, (-0.1).sp),
        titleSmall = style(14, 20, FontWeight.SemiBold),
        bodyLarge = style(16, 24, FontWeight.Normal),
        bodyMedium = style(14, 20, FontWeight.Normal),
        bodySmall = style(12, 16, FontWeight.Normal, 0.1.sp),
        labelLarge = style(14, 20, FontWeight.SemiBold, 0.1.sp),
        labelMedium = style(12, 16, FontWeight.SemiBold, 0.2.sp),
        labelSmall = style(11, 14, FontWeight.Medium, 0.3.sp),
    )
