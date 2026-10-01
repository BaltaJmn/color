package com.baltajmn.color.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val MAX_CONTENT_WIDTH = 560.dp
val GUTTER = 20.dp
val CARD_RADIUS = 28.dp

/**
 * Achromatic on purpose (docs/pantallas.md 1): a tinted gray shifts how the color on top of it is
 * seen, which is why a viewing booth is plain gray. The only color on screen is the one the user
 * picked; primary is the text color, so a button reads as ink, never as a brand.
 */
internal val Light = lightColorScheme(
    primary = Color(0xFF111111),
    onPrimary = Color(0xFFF1F1F1),
    secondary = Color(0xFF5E5E5E),
    background = Color(0xFFF1F1F1),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFE6E6E6),
    onSurfaceVariant = Color(0xFF5E5E5E),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    // Every role Material reaches for on its own (the time picker's fields and dial, menus), set to
    // the same grays: left out, they come in as Material's lavender.
    primaryContainer = Color(0xFFD0D0D0),
    onPrimaryContainer = Color(0xFF111111),
    secondaryContainer = Color(0xFFE6E6E6),
    onSecondaryContainer = Color(0xFF111111),
    tertiary = Color(0xFF5E5E5E),
    onTertiary = Color(0xFFF1F1F1),
    tertiaryContainer = Color(0xFFE6E6E6),
    onTertiaryContainer = Color(0xFF111111),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFE6E6E6),
    inverseSurface = Color(0xFF111111),
    inverseOnSurface = Color(0xFFF1F1F1),
    inversePrimary = Color(0xFFF2F2F2),
    outline = Color(0xFFD6D6D6),
    outlineVariant = Color(0xFFEAEAEA),
    error = Color(0xFFB3261E),
)

internal val Dark = darkColorScheme(
    primary = Color(0xFFF2F2F2),
    onPrimary = Color(0xFF0E0E0E),
    secondary = Color(0xFFA3A3A3),
    background = Color(0xFF0E0E0E),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF252525),
    onSurfaceVariant = Color(0xFFA3A3A3),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF3A3A3A),
    onPrimaryContainer = Color(0xFFF2F2F2),
    secondaryContainer = Color(0xFF252525),
    onSecondaryContainer = Color(0xFFF2F2F2),
    tertiary = Color(0xFFA3A3A3),
    onTertiary = Color(0xFF0E0E0E),
    tertiaryContainer = Color(0xFF252525),
    onTertiaryContainer = Color(0xFFF2F2F2),
    surfaceContainerLowest = Color(0xFF0E0E0E),
    surfaceContainerLow = Color(0xFF1A1A1A),
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF252525),
    inverseSurface = Color(0xFFF2F2F2),
    inverseOnSurface = Color(0xFF0E0E0E),
    inversePrimary = Color(0xFF111111),
    outline = Color(0xFF303030),
    outlineVariant = Color(0xFF222222),
    error = Color(0xFFF2B8B5),
)

private val SoftShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(CARD_RADIUS),
    extraLarge = RoundedCornerShape(CARD_RADIUS),
)

@Composable
fun ChromaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, shapes = SoftShapes, content = content)
}

/**
 * The styles of docs/pantallas.md 1, in the system font: the color is the protagonist. The voice
 * comes from weight and spacing instead: numerals huge and light like the readout of an instrument,
 * small labels in tracked capitals, and codes in tabular figures like the one printed on a paint chip.
 */
object Styles {
    val numeral: TextStyle @Composable get() = system(64.sp, 64.sp, FontWeight.ExtraLight, colors.onBackground, (-2.5).sp)
    val display: TextStyle @Composable get() = system(34.sp, 40.sp, FontWeight.Medium, colors.onBackground, (-0.6).sp)
    val title: TextStyle @Composable get() = system(22.sp, 28.sp, FontWeight.SemiBold, colors.onBackground, (-0.3).sp)
    val body: TextStyle @Composable get() = system(17.sp, 24.sp, FontWeight.Normal, colors.onBackground)
    val label: TextStyle @Composable get() = system(14.sp, 20.sp, FontWeight.Medium, colors.onSurfaceVariant)
    val caption: TextStyle @Composable get() = system(13.sp, 18.sp, FontWeight.Normal, colors.onSurfaceVariant)
    val muted: TextStyle @Composable get() = system(17.sp, 24.sp, FontWeight.Normal, colors.onSurfaceVariant)
    /** Section names and the weekday: say what follows, never compete with it. Upper-case at the call site. */
    val eyebrow: TextStyle @Composable get() = system(12.sp, 16.sp, FontWeight.SemiBold, colors.onSurfaceVariant, 1.4.sp)
    val code: TextStyle @Composable get() = system(14.sp, 20.sp, FontWeight.Medium, colors.onSurfaceVariant, 1.sp)
        .copy(fontFeatureSettings = "tnum")

    private val colors @Composable get() = MaterialTheme.colorScheme

    private fun system(size: TextUnit, line: TextUnit, weight: FontWeight, color: Color, tracking: TextUnit = TextUnit.Unspecified) =
        TextStyle(fontSize = size, lineHeight = line, fontWeight = weight, color = color, letterSpacing = tracking)
}

/**
 * Top and sides only: the bottom bar already sits above the system bar, so a screen over it that
 * padded the bottom too would leave the gap twice. The keyboard still pushes content up.
 */
@Composable
fun Modifier.screenInsets(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)).imePadding()
