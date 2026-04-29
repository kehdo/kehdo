package app.kehdo.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Root theme wrapper for the kehdo app.
 * Applies the Aurora palette + Inter typography.
 *
 * Aurora is a dark-first design system, so dark colors are the default.
 * A light theme will be added in Phase 2.
 */
@Composable
fun KehdoTheme(
    darkTheme: Boolean = true, // Aurora is dark by default; ignore system pref for v1
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = AuroraColors.Purple,
        onPrimary = AuroraColors.Text,
        secondary = AuroraColors.Pink,
        onSecondary = AuroraColors.Text,
        tertiary = AuroraColors.Amber,
        background = AuroraColors.Canvas,
        onBackground = AuroraColors.Text,
        surface = AuroraColors.Surface,
        onSurface = AuroraColors.Text,
        error = AuroraColors.Pink,
        onError = AuroraColors.Text
    )

    CompositionLocalProvider(LocalAuroraColors provides AuroraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KehdoTypography,
            content = content
        )
    }
}

val LocalAuroraColors = staticCompositionLocalOf { AuroraColors }
