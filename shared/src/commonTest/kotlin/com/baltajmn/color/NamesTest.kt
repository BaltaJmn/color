package com.baltajmn.color

import com.baltajmn.color.color.COLOR_NAMES
import com.baltajmn.color.color.colorLabel
import com.baltajmn.color.color.deltaE
import com.baltajmn.color.color.hexOf
import com.baltajmn.color.color.labOf
import com.baltajmn.color.color.nearestName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Test 8.
class NamesTest {

    @Test
    fun everyColorNamesItself() {
        for (c in COLOR_NAMES) assertEquals(c.key, nearestName(c.hex).key, c.key)
    }

    @Test
    fun keysAreUniqueAndEveryLanguageIsFilled() {
        assertEquals(COLOR_NAMES.size, COLOR_NAMES.map { it.key }.toSet().size)
        for (c in COLOR_NAMES) for (lang in listOf("en", "es", "pt", "de", "fr")) assertTrue(c.label(lang).isNotBlank())
    }

    @Test
    fun noTwoNamesForAlmostTheSameColor() {
        for (i in COLOR_NAMES.indices) for (j in i + 1 until COLOR_NAMES.size) {
            val d = deltaE(COLOR_NAMES[i].lab, COLOR_NAMES[j].lab)
            assertTrue(d >= 5.0, "${COLOR_NAMES[i].key} and ${COLOR_NAMES[j].key} are $d apart")
        }
    }

    @Test
    fun greysGetNeutralNames() {
        for (v in listOf(0x20, 0x50, 0x80, 0xA0, 0xC8, 0xF0)) {
            val grey = hexOf((v shl 16) or (v shl 8) or v)
            assertTrue(labOf(nearestName(grey).hex).chroma < 10.0, "$grey is ${nearestName(grey).key}")
        }
    }

    @Test
    fun labelsAreCapitalized() {
        assertEquals("Azul tormenta", colorLabel("storm_blue", "es"))
        assertEquals("Mystery", colorLabel("mystery", "es"))
    }
}
