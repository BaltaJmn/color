package com.baltajmn.color.i18n

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.datetime.LocalDate

class StringsTest {
    private val saved = S.lang
    private val sep22 = LocalDate(2026, 9, 22)

    @AfterTest
    fun restore() {
        S.lang = saved
    }

    private fun each(check: () -> String, en: String, es: String, pt: String, de: String, fr: String) {
        listOf("en" to en, "es" to es, "pt" to pt, "de" to de, "fr" to fr).forEach { (code, expected) ->
            S.lang = code
            assertEquals(expected, check(), code)
        }
    }

    @Test
    fun fallsBackToEnglish() {
        assertEquals("es", normalizeLanguage("es-ES"))
        assertEquals("en", normalizeLanguage("it"))
        assertEquals("en", normalizeLanguage(""))
    }

    @Test
    fun dates() {
        each(
            { S.longDate(sep22) },
            "Tuesday, September 22", "Martes, 22 de septiembre", "Terça-feira, 22 de setembro",
            "Dienstag, 22. September", "Mardi 22 septembre",
        )
        each(
            { S.abbrDateWithYear(sep22) },
            "Sep 22, 2026", "22 sept 2026", "22 set 2026", "22. Sept. 2026", "22 sept. 2026",
        )
    }

    @Test
    fun colorNamesFollowTheLanguage() {
        each({ S.colorName("storm_blue") }, "Storm blue", "Azul tormenta", "Azul tempestade", "Sturmblau", "Bleu orage")
        each(
            { S.a11yDay(sep22, null) },
            "September 22, no color", "22 de septiembre, sin color", "22 de setembro, sem cor",
            "22. September, keine Farbe", "22 septembre, pas de couleur",
        )
    }

    @Test
    fun importSummary() {
        S.lang = "es"
        assertEquals("La copia trae 1 día nuevo. No se borra nada.", S.importSummary(1, 0))
        assertEquals(
            "La copia trae 12 días nuevos. Los días que ya están en este teléfono se quedan como están.",
            S.importSummary(12, 3),
        )
    }

    @Test
    fun noTypographicDashesOrQuotes() {
        val banned = listOf('—', '–', '“', '”', '‘', '’', '…')
        for (code in SUPPORTED) {
            S.lang = code
            val sample = listOf(
                S.todayPrompt, S.firstHelp, S.galleryNotToday, S.noticeCorrupt, S.importSummary(2, 1),
                S.proOnce, S.reminderText, S.watermarkRow, S.importTooNew, S.siblingQuilt,
            )
            for (text in sample) for (c in banned) assertFalse(c in text, "$code: $text")
        }
    }
}
