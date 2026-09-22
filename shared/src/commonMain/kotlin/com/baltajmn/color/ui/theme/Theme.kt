package com.baltajmn.color.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
 * Neutral on purpose (docs/pantallas.md 1): the only vivid color on screen is the one the user
 * picked. primary is the text color, so a Material button reads as ink on paper, never as a brand.
 */
internal val Light = lightColorScheme(
    primary = Color(0xFF1C1B1A),
    onPrimary = Color(0xFFF6F4F1),
    secondary = Color(0xFF6E6A64),
    background = Color(0xFFF6F4F1),
    onBackground = Color(0xFF1C1B1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFECE9E4),
    onSurfaceVariant = Color(0xFF6E6A64),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    outline = Color(0xFFD9D5CF),
    outlineVariant = Color(0xFFECE9E4),
    error = Color(0xFFB3261E),
)

internal val Dark = darkColorScheme(
    primary = Color(0xFFEDEAE6),
    onPrimary = Color(0xFF141312),
    secondary = Color(0xFFA29D96),
    background = Color(0xFF141312),
    onBackground = Color(0xFFEDEAE6),
    surface = Color(0xFF1E1D1B),
    onSurface = Color(0xFFEDEAE6),
    surfaceVariant = Color(0xFF2A2826),
    onSurfaceVariant = Color(0xFFA29D96),
    surfaceContainerHigh = Color(0xFF1E1D1B),
    outline = Color(0xFF3A3734),
    outlineVariant = Color(0xFF2A2826),
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
    val body: TextStyle @Composable get() = system(16.sp, 22.sp, FontWeight.Normal, colors.onBackground)
    val label: TextStyle @Composable get() = system(13.sp, 18.sp, FontWeight.Medium, colors.onSurfaceVariant)
    val caption: TextStyle @Composable get() = system(12.sp, 16.sp, FontWeight.Normal, colors.onSurfaceVariant)
    val muted: TextStyle @Composable get() = system(16.sp, 22.sp, FontWeight.Normal, colors.onSurfaceVariant)

    private val colors @Composable get() = MaterialTheme.colorScheme

    private fun system(size: TextUnit, line: TextUnit, weight: FontWeight, color: Color) =
        TextStyle(fontSize = size, lineHeight = line, fontWeight = weight, color = color)
}
