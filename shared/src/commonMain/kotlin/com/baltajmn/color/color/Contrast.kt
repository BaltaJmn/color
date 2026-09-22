package com.baltajmn.color.color

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/** WCAG relative luminance of an opaque 0xFFRRGGBB. */
fun luminance(rgb: Int): Double {
    fun ch(v: Int): Double {
        val c = v / 255.0
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * ch((rgb shr 16) and 0xFF) + 0.7152 * ch((rgb shr 8) and 0xFF) + 0.0722 * ch(rgb and 0xFF)
}

fun contrast(a: Int, b: Int): Double {
    val la = luminance(a)
    val lb = luminance(b)
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
}

private const val WHITE = 0xFFFFFFFF.toInt()
private const val BLACK = 0xFF000000.toInt()

/**
 * The ink for text on [hex]: pure white or pure black, whichever contrasts more. With those two the
 * worst case over all of sRGB is 4.58:1, so every text on a card passes AA (docs/tecnico.md 6.5).
 */
fun inkFor(hex: String): Int {
    val rgb = rgbOf(hex)
    return if (contrast(rgb, WHITE) >= contrast(rgb, BLACK)) WHITE else BLACK
}

fun colorOf(hex: String): Color = Color(rgbOf(hex))

fun inkColorFor(hex: String): Color = Color(inkFor(hex))
