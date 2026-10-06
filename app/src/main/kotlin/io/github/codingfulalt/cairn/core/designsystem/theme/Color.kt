package io.github.codingfulalt.cairn.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.github.codingfulalt.cairn.core.model.HabitColor

object CairnPalette {
    val Ink = Color(0xFF1F2124)
    val Paper = Color(0xFFF4F4F2)
    val Night = Color(0xFF141415)
    val Fog = Color(0xFFF2F1EE)
    val Blaze = Color(0xFFD63A2F)
    val BlazeBright = Color(0xFFE5483C)
}

val HabitColor.swatch: Color
    get() =
        when (this) {
            HabitColor.Moss -> Color(0xFFCCDDB5)
            HabitColor.Mint -> Color(0xFFBDE2D0)
            HabitColor.Sky -> Color(0xFFC4D9F1)
            HabitColor.Lavender -> Color(0xFFD6CEF1)
            HabitColor.Rose -> Color(0xFFF5CAD2)
            HabitColor.Peach -> Color(0xFFF7D0B5)
            HabitColor.Sand -> Color(0xFFEEDFB9)
            HabitColor.Coral -> Color(0xFFF2B2A5)
        }

internal val LightColors =
    lightColorScheme(
        primary = CairnPalette.Ink,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFF7DCD8),
        onPrimaryContainer = Color(0xFF7A1A10),
        secondary = CairnPalette.Ink,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8E7E2),
        onSecondaryContainer = CairnPalette.Ink,
        tertiary = CairnPalette.Blaze,
        onTertiary = Color.White,
        background = CairnPalette.Paper,
        onBackground = CairnPalette.Ink,
        surface = CairnPalette.Paper,
        onSurface = CairnPalette.Ink,
        surfaceVariant = Color(0xFFE8E7E2),
        onSurfaceVariant = Color(0xFF6E6D69),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFFAFAF8),
        surfaceContainer = Color.White,
        surfaceContainerHigh = Color(0xFFECEBE7),
        surfaceContainerHighest = Color(0xFFE3E2DD),
        outline = Color(0xFFCFCEC8),
        outlineVariant = Color(0xFFE2E1DC),
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        inverseSurface = CairnPalette.Ink,
        inverseOnSurface = CairnPalette.Fog,
        inversePrimary = CairnPalette.Blaze,
    )

internal val DarkColors =
    darkColorScheme(
        primary = CairnPalette.Fog,
        onPrimary = CairnPalette.Night,
        primaryContainer = Color(0xFF4A2521),
        onPrimaryContainer = Color(0xFFFFB4A9),
        secondary = CairnPalette.Fog,
        onSecondary = CairnPalette.Night,
        secondaryContainer = Color(0xFF2A2A2D),
        onSecondaryContainer = CairnPalette.Fog,
        tertiary = CairnPalette.BlazeBright,
        onTertiary = Color.White,
        background = CairnPalette.Night,
        onBackground = CairnPalette.Fog,
        surface = CairnPalette.Night,
        onSurface = CairnPalette.Fog,
        surfaceVariant = Color(0xFF26262A),
        onSurfaceVariant = Color(0xFFA3A29D),
        surfaceContainerLowest = Color(0xFF0D0D0E),
        surfaceContainerLow = Color(0xFF19191B),
        surfaceContainer = Color(0xFF1F1F21),
        surfaceContainerHigh = Color(0xFF27272A),
        surfaceContainerHighest = Color(0xFF303033),
        outline = Color(0xFF3E3E42),
        outlineVariant = Color(0xFF2C2C2F),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        inverseSurface = CairnPalette.Fog,
        inverseOnSurface = CairnPalette.Ink,
        inversePrimary = CairnPalette.Ink,
    )
