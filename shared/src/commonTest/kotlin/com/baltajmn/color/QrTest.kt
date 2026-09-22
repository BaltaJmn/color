package com.baltajmn.color

import com.baltajmn.color.social.inviteCodeOf
import com.baltajmn.color.social.inviteLink
import com.baltajmn.color.social.qrEncode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// 15. docs/tecnico.md 6.10. The reference is what CoreImage's CIQRCodeGenerator draws for the same
// link at level M, module for module; the same comparison held for versions 1, 4, 5 and 6.
class QrTest {

    private val reference = listOf(
        "11111110000110001111001111111",
        "10000010010100111100001000001",
        "10111010101000000011101011101",
        "10111010111001011101001011101",
        "10111010100000100011101011101",
        "10000010111110111000101000001",
        "11111110101010101010101111111",
        "00000000100010000010100000000",
        "10111110000011110101001111100",
        "11011100010100001011101110001",
        "01000011110110110000010100000",
        "10011100101100100001011101010",
        "00101010100110011101010001100",
        "00000001011111000001111110001",
        "01010011000011110100101011100",
        "00010101010101000010011100010",
        "11110110011010110110000001100",
        "11110001010000001111111110101",
        "10011010011100011100010100100",
        "10101000011110110000111100010",
        "10001010111000011100111110111",
        "00000000101101100111100011111",
        "11111110001111111011101011100",
        "10000010100011010001100010001",
        "10111010100100110110111110100",
        "10111010100110001110000001111",
        "10111010111001110101111111110",
        "10000010010000010000110101010",
        "11111110110010111111000010100",
    )

    @Test
    fun anInviteLinkMatchesTheReferenceMatrix() {
        val qr = qrEncode("https://color.baltajmn.dev/i/0a1b2c3d4e")!!
        assertEquals(29, qr.size)
        val drawn = (0 until qr.size).map { y -> (0 until qr.size).joinToString("") { x -> if (qr[x, y]) "1" else "0" } }
        assertEquals(reference, drawn)
    }

    @Test
    fun theSizeGrowsWithTheTextAndStopsAtVersionSix() {
        assertEquals(21, qrEncode("chroma")!!.size)
        assertEquals(41, qrEncode("x".repeat(106))!!.size)
        assertNull(qrEncode("x".repeat(107)))
    }

    @Test
    fun onlyAnInviteLinkGivesACode() {
        assertEquals("0a1b2c3d4e", inviteCodeOf("https://color.baltajmn.dev/i/0A1B2C3D4E/"))
        assertEquals("0a1b2c3d4e", inviteCodeOf(inviteLink("0a1b2c3d4e") + "?utm=x"))
        assertNull(inviteCodeOf("https://color.baltajmn.dev/i/0a1b2c3d"))
        assertNull(inviteCodeOf("https://evil.dev/i/0a1b2c3d4e"))
        assertNull(inviteCodeOf("https://color.baltajmn.dev.evil.dev/i/0a1b2c3d4e"))
    }
}
