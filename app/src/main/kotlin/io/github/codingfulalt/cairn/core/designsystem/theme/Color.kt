package io.github.codingfulalt.cairn.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.github.codingfulalt.cairn.core.model.HabitColor

object CairnPalette {
    val Ink = Color(0xFF14151A)
    val Paper = Color(0xFFF5F4EF)
    val Night = Color(0xFF0E0F12)
    val Lime = Color(0xFFD7F55A)
    val Fog = Color(0xFFF2F2EE)
}

val HabitColor.swatch: Color
    get() =
        when (this) {
            HabitColor.Moss -> Color(0xFFD7F55A)
            HabitColor.Mint -> Color(0xFF9BE8C8)
            HabitColor.Sky -> Color(0xFF9CCBFF)
            HabitColor.Lavender -> Color(0xFFC3B5FF)
            HabitColor.Rose -> Color(0xFFFFB3C7)
            HabitColor.Peach -> Color(0xFFFFC49B)
            HabitColor.Sand -> Color(0xFFF3DF95)
            HabitColor.Coral -> Color(0xFFFF9C8A)
        }

internal val LightColors =
    lightColorScheme(
        primary = CairnPalette.Ink,
        onPrimary = Color.White,
        primaryContainer = CairnPalette.Lime,
        onPrimaryContainer = CairnPalette.Ink,
        secondary = CairnPalette.Ink,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8E7DF),
        onSecondaryContainer = CairnPalette.Ink,
        tertiary = Color(0xFF6B5BD6),
        onTertiary = Color.White,
        background = CairnPalette.Paper,
        onBackground = CairnPalette.Ink,
        surface = CairnPalette.Paper,
        onSurface = CairnPalette.Ink,
        surfaceVariant = Color(0xFFE9E8E1),
        onSurfaceVariant = Color(0xFF63646B),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFFBFAF7),
        surfaceContainer = Color.White,
        surfaceContainerHigh = Color(0xFFEFEEE8),
        surfaceContainerHighest = Color(0xFFE6E5DE),
        outline = Color(0xFFCFCEC6),
        outlineVariant = Color(0xFFE2E1DA),
        error = Color(0xFFD6453D),
        onError = Color.White,
        inverseSurface = CairnPalette.Ink,
        inverseOnSurface = CairnPalette.Fog,
        inversePrimary = CairnPalette.Lime,
    )

internal val DarkColors =
    darkColorScheme(
        primary = CairnPalette.Lime,
        onPrimary = CairnPalette.Ink,
        primaryContainer = CairnPalette.Lime,
        onPrimaryContainer = CairnPalette.Ink,
        secondary = CairnPalette.Lime,
        onSecondary = CairnPalette.Ink,
        secondaryContainer = Color(0xFF2A2B31),
        onSecondaryContainer = CairnPalette.Fog,
        tertiary = Color(0xFFB9ADFF),
        onTertiary = CairnPalette.Ink,
        background = CairnPalette.Night,
        onBackground = CairnPalette.Fog,
        surface = CairnPalette.Night,
        onSurface = CairnPalette.Fog,
        surfaceVariant = Color(0xFF24252B),
        onSurfaceVariant = Color(0xFFA3A4AB),
        surfaceContainerLowest = Color(0xFF0A0B0D),
        surfaceContainerLow = Color(0xFF141519),
        surfaceContainer = Color(0xFF18191E),
        surfaceContainerHigh = Color(0xFF202127),
        surfaceContainerHighest = Color(0xFF2A2B32),
        outline = Color(0xFF3D3E45),
        outlineVariant = Color(0xFF2C2D33),
        error = Color(0xFFFF7A70),
        onError = CairnPalette.Ink,
        inverseSurface = CairnPalette.Fog,
        inverseOnSurface = CairnPalette.Ink,
        inversePrimary = CairnPalette.Ink,
    )
