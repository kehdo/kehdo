package app.kehdo.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * kehdo typography scale — generated from /design/tokens/typography.json
 *
 * Primary: Inter (loaded via Google Fonts in production)
 * Accent (gradient headlines): Instrument Serif Italic — applied per-component, not in scale
 *
 * For the scaffold we use FontFamily.Default; in Phase 0 the generator will
 * set up Inter via androidx.compose.ui.text.googlefonts.
 *
 * All TextUnits use `.sp` — never `.em`. Compose's M3 components (e.g.
 * OutlinedTextField) lerp between styles we override and styles we don't,
 * and the M3 defaults are all `.sp`. Mixing `.em` and `.sp` in the same
 * theme makes those lerps throw `IllegalArgumentException: Cannot perform
 * operation for Em and Sp`.
 */
val KehdoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 96.sp,
        lineHeight = 91.sp,
        letterSpacing = (-3.8).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 72.sp,
        lineHeight = 68.sp,
        letterSpacing = (-2.9).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 53.sp,
        letterSpacing = (-1.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 41.sp,
        letterSpacing = (-0.7).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.2).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    )
)
