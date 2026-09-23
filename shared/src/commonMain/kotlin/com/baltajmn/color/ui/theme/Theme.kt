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
    onPrimary = Color(0xFFF3F3F3),
    secondary = Color(0xFF595959),
    background = Color(0xFFF3F3F3),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFE5E5E5),
    onSurfaceVariant = Color(0xFF595959),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    outline = Color(0xFFD0D0D0),
    outlineVariant = Color(0xFFE5E5E5),
    error = Color(0xFFB3261E),
)

internal val Dark = darkColorScheme(
    primary = Color(0xFFF2F2F2),
    onPrimary = Color(0xFF121212),
    secondary = Color(0xFFA8A8A8),
    background = Color(0xFF121212),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF1C1C1C),
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFFA8A8A8),
    surfaceContainerHigh = Color(0xFF1C1C1C),
    outline = Color(0xFF333333),
    outlineVariant = Color(0xFF262626),
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

/** The styles of docs/pantallas.md 1, in the system font: the color is the protagonist. */
object Styles {
    val display: TextStyle @Composable get() = system(34.sp, 40.sp, FontWeight.Medium, colors.onBackground)
    val title: TextStyle @Composable get() = system(22.sp, 28.sp, FontWeight.Medium, colors.onBackground)
    val body: TextStyle @Composable get() = system(17.sp, 24.sp, FontWeight.Normal, colors.onBackground)
    val label: TextStyle @Composable get() = system(14.sp, 20.sp, FontWeight.Medium, colors.onSurfaceVariant)
    val caption: TextStyle @Composable get() = system(13.sp, 18.sp, FontWeight.Normal, colors.onSurfaceVariant)
    val muted: TextStyle @Composable get() = system(17.sp, 24.sp, FontWeight.Normal, colors.onSurfaceVariant)

    private val colors @Composable get() = MaterialTheme.colorScheme

    private fun system(size: TextUnit, line: TextUnit, weight: FontWeight, color: Color) =
        TextStyle(fontSize = size, lineHeight = line, fontWeight = weight, color = color)
}

/**
 * Top and sides only: the bottom bar already sits above the system bar, so a screen over it that
 * padded the bottom too would leave the gap twice. The keyboard still pushes content up.
 */
@Composable
fun Modifier.screenInsets(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)).imePadding()
