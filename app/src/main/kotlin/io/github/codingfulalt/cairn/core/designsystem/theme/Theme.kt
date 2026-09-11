package io.github.codingfulalt.cairn.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class CairnColors(
    val accent: Color,
    val onAccent: Color,
    val navBar: Color,
    val navBarContent: Color,
    val navBarSelected: Color,
    val isDark: Boolean,
)

private val LightCairnColors =
    CairnColors(
        accent = CairnPalette.Lime,
        onAccent = CairnPalette.Ink,
        navBar = CairnPalette.Ink,
        navBarContent = Color(0xFF9A9BA2),
        navBarSelected = CairnPalette.Lime,
        isDark = false,
    )

private val DarkCairnColors =
    CairnColors(
        accent = CairnPalette.Lime,
        onAccent = CairnPalette.Ink,
        navBar = Color(0xFF212228),
        navBarContent = Color(0xFF8E8F96),
        navBarSelected = CairnPalette.Lime,
        isDark = true,
    )

private val LocalCairnColors = staticCompositionLocalOf { LightCairnColors }

private val CairnShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(14.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(36.dp),
    )

@Composable
fun CairnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(
        LocalCairnColors provides if (darkTheme) DarkCairnColors else LightCairnColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CairnTypography,
            shapes = CairnShapes,
        ) {
            // without this, text outside a Surface falls back to black, even in dark mode
            CompositionLocalProvider(
                LocalContentColor provides colorScheme.onBackground,
                content = content,
            )
        }
    }
}

object CairnTheme {
    val colors: CairnColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCairnColors.current
}
