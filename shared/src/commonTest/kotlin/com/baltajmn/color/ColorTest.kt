package com.baltajmn.color

import com.baltajmn.color.color.deltaE
import com.baltajmn.color.color.extractSwatches
import com.baltajmn.color.color.hexOf
import com.baltajmn.color.color.labOf
import com.baltajmn.color.color.rgbOf
import com.baltajmn.color.color.toRgb
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorTest {

    // Test 2: to Lab and back, within one step per channel.
    @Test
    fun labRoundTrips() {
        val random = Random(1)
        repeat(1000) {
            val rgb = (0xFF shl 24) or random.nextInt(0x1000000)
            val back = labOf(rgb).toRgb()
            for (shift in listOf(0, 8, 16)) {
                assertTrue(abs(((rgb shr shift) and 0xFF) - ((back shr shift) and 0xFF)) <= 1, hexOf(rgb))
            }
        }
    }

    // Test 3: reference values.
    @Test
    fun referenceValues() {
        val red = labOf("#FF0000")
        assertEquals(53.24, red.l, 0.05)
        assertEquals(80.09, red.a, 0.05)
        assertEquals(67.20, red.b, 0.05)
        assertEquals(100.0, deltaE(labOf("#FFFFFF"), labOf("#000000")), 0.01)
        assertEquals(0.0, labOf("#808080").chroma, 0.01)
        assertEquals("#0A0B0C", hexOf(rgbOf("#0a0b0c")))
    }

    private fun bands(vararg colors: String, side: Int = 64): IntArray {
        val rgb = colors.map(::rgbOf)
        return IntArray(side * side) { i -> rgb[(i % side) * rgb.size / side] }
    }

    // Test 4: same pixels, same swatches.
    @Test
    fun deterministic() {
        val random = Random(3)
        val pixels = IntArray(64 * 64) { (0xFF shl 24) or random.nextInt(0x1000000) }
        assertEquals(extractSwatches(pixels), extractSwatches(pixels.copyOf()))
    }

    // Test 5: three bands give those three colors.
    @Test
    fun threeBands() {
        val colors = listOf("#D32F2F", "#1976D2", "#FBC02D")
        val swatches = extractSwatches(bands(*colors.toTypedArray()))
        assertEquals(3, swatches.size)
        for (c in colors) assertTrue(swatches.any { deltaE(labOf(it.color), labOf(c)) < 3 }, c)
    }

    // Test 6: a grey photo with a little red still offers the red.
    @Test
    fun vividAlwaysOffered() {
        val side = 64
        val grey = rgbOf("#8A8580")
        val red = rgbOf("#C62828")
        val pixels = IntArray(side * side) { i -> if (i < side * side * 3 / 100) red else grey }
        val swatches = extractSwatches(pixels)
        assertTrue(swatches.any { deltaE(labOf(it.color), labOf(red)) < 3 })
    }

    @Test
    fun monochromeOffersOne() {
        assertEquals(1, extractSwatches(bands("#556B2F")).size)
    }

    @Test
    fun neverMoreThanFiveAndNeverTwins() {
        val random = Random(9)
        val pixels = IntArray(64 * 64) { (0xFF shl 24) or random.nextInt(0x1000000) }
        val swatches = extractSwatches(pixels)
        assertTrue(swatches.size in 1..5)
        for (i in swatches.indices) for (j in i + 1 until swatches.size) {
            assertTrue(deltaE(labOf(swatches[i].color), labOf(swatches[j].color)) >= 9.0)
        }
    }

    @Test
    fun transparentPixelsAreIgnored() {
        assertTrue(extractSwatches(IntArray(16) { 0x00FF0000 }).isEmpty())
    }
}
