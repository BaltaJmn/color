package com.baltajmn.color.color

import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** CIELAB under D65. Every distance in the app is measured here, with the same formula. */
data class Lab(val l: Double, val a: Double, val b: Double) {
    val chroma: Double get() = sqrt(a * a + b * b)
}

/** CIE76: plain euclidean distance in Lab. Enough for "these two look alike", and easy to test. */
fun deltaE(x: Lab, y: Lab): Double {
    val dl = x.l - y.l
    val da = x.a - y.a
    val db = x.b - y.b
    return sqrt(dl * dl + da * da + db * db)
}

private const val XN = 0.95047
private const val YN = 1.0
private const val ZN = 1.08883
private const val EPSILON = 216.0 / 24389.0 // (6/29)^3
private const val KAPPA = 24389.0 / 27.0

private val LINEAR = DoubleArray(256) { i ->
    val c = i / 255.0
    if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
}

private fun f(t: Double) = if (t > EPSILON) cbrt(t) else (KAPPA * t + 16.0) / 116.0

private fun fInv(t: Double): Double {
    val t3 = t * t * t
    return if (t3 > EPSILON) t3 else (116.0 * t - 16.0) / KAPPA
}

private fun encode(linear: Double): Int {
    val c = if (linear <= 0.0031308) 12.92 * linear else 1.055 * linear.pow(1.0 / 2.4) - 0.055
    return (c * 255.0).roundToInt().coerceIn(0, 255)
}

/** An opaque sRGB color (alpha ignored) in Lab. */
fun labOf(rgb: Int): Lab {
    val r = LINEAR[(rgb shr 16) and 0xFF]
    val g = LINEAR[(rgb shr 8) and 0xFF]
    val b = LINEAR[rgb and 0xFF]
    val x = (0.4124564 * r + 0.3575761 * g + 0.1804375 * b) / XN
    val y = (0.2126729 * r + 0.7151522 * g + 0.0721750 * b) / YN
    val z = (0.0193339 * r + 0.1191920 * g + 0.9503041 * b) / ZN
    val fx = f(x)
    val fy = f(y)
    val fz = f(z)
    return Lab(116.0 * fy - 16.0, 500.0 * (fx - fy), 200.0 * (fy - fz))
}

/** Back to an opaque 0xFFRRGGBB, clipped to the sRGB gamut. */
fun Lab.toRgb(): Int {
    val fy = (l + 16.0) / 116.0
    val fx = fy + a / 500.0
    val fz = fy - b / 200.0
    val x = fInv(fx) * XN
    val y = fInv(fy) * YN
    val z = fInv(fz) * ZN
    val r = encode(3.2404542 * x - 1.5371385 * y - 0.4985314 * z)
    val g = encode(-0.9692660 * x + 1.8760108 * y + 0.0415560 * z)
    val bl = encode(0.0556434 * x - 0.2040259 * y + 1.0572252 * z)
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or bl
}

/** "#RRGGBB", upper case, the only way a color is written down. */
fun hexOf(rgb: Int): String = "#" + (rgb and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')

/** Opaque 0xFFRRGGBB from "#RRGGBB". */
fun rgbOf(hex: String): Int = (0xFF shl 24) or hex.removePrefix("#").toInt(16)

fun labOf(hex: String): Lab = labOf(rgbOf(hex))
