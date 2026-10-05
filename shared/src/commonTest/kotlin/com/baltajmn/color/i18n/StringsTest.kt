package com.baltajmn.color.i18n

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class StringsTest {
    private val saved = S.lang
    private val sep22 = LocalDate(2026, 9, 22)

    @AfterTest
    fun restore() {
        S.lang = saved
    }

    /** One expected value per language, in the order of [SUPPORTED]. */
    private fun each(check: () -> String, vararg expected: String) {
        assertEquals(SUPPORTED.size, expected.size)
        SUPPORTED.zip(expected).forEach { (code, value) ->
            S.lang = code
            assertEquals(value, check(), code)
        }
    }

    @Test
    fun fallsBackToEnglish() {
        assertEquals("es", normalizeLanguage("es-ES"))
        assertEquals("it", normalizeLanguage("it"))
        assertEquals("en", normalizeLanguage("sv"))
        assertEquals("en", normalizeLanguage(""))
        // Android's Locale still says "in" for Indonesian.
        assertEquals("id", normalizeLanguage("in"))
        assertEquals("id", normalizeLanguage("id-ID"))
    }

    @Test
    fun dates() {
        each(
            { S.longDate(sep22) },
            "Tuesday, September 22", "Martes, 22 de septiembre", "Terça-feira, 22 de setembro",
            "Dienstag, 22. September", "Mardi 22 septembre", "Martedì 22 settembre", "Dinsdag 22 september",
            "Wtorek, 22 września", "Вторник, 22 сентября", "22 Eylül Salı", "Selasa, 22 September",
            "9月22日 火曜日", "9월 22일 화요일",
        )
        each(
            { S.abbrDateWithYear(sep22) },
            "Sep 22, 2026", "22 sept 2026", "22 set 2026", "22. Sept. 2026", "22 sept. 2026", "22 set 2026",
            "22 sep 2026", "22 wrz 2026", "22 сент. 2026", "22 Eyl 2026", "22 Sep 2026", "2026年9月22日", "2026년 9월 22일",
        )
        each(
            { S.longDateWithYear(sep22) },
            "Tuesday, September 22, 2026", "Martes, 22 de septiembre de 2026", "Terça-feira, 22 de setembro de 2026",
            "Dienstag, 22. September 2026", "Mardi 22 septembre 2026", "Martedì 22 settembre 2026",
            "Dinsdag 22 september 2026", "Wtorek, 22 września 2026", "Вторник, 22 сентября 2026",
            "22 Eylül 2026 Salı", "Selasa, 22 September 2026", "2026年9月22日 火曜日", "2026년 9월 22일 화요일",
        )
    }

    @Test
    fun colorNamesFollowTheLanguage() {
        each(
            { S.colorName("storm_blue") },
            "Storm blue", "Azul tormenta", "Azul tempestade", "Sturmblau", "Bleu orage", "Blu tempesta", "Stormblauw",
            "Burzowy błękit", "Грозовой синий", "Fırtına mavisi", "Biru badai", "ストームブルー", "스톰 블루",
        )
        each(
            { S.a11yDay(sep22, null) },
            "September 22, no color", "22 de septiembre, sin color", "22 de setembro, sem cor",
            "22. September, keine Farbe", "22 septembre, pas de couleur", "22 settembre, nessun colore",
            "22 september, geen kleur", "22 września, bez koloru", "22 сентября, без цвета", "22 Eylül, renk yok",
            "22 September, tanpa warna", "9月22日, 色なし", "9월 22일, 색 없음",
        )
    }

    @Test
    fun slavicPluralsHaveThreeForms() {
        S.lang = "ru"
        assertEquals("В копии 1 новый день. Ничего не удаляется.", S.importSummary(1, 0))
        assertEquals("В копии 3 новых дня. Ничего не удаляется.", S.importSummary(3, 0))
        assertEquals("В копии 11 новых дней. Ничего не удаляется.", S.importSummary(11, 0))
        assertEquals("В копии 21 новый день. Ничего не удаляется.", S.importSummary(21, 0))
        S.lang = "pl"
        assertEquals("Kopia zawiera 1 nowy dzień. Nic nie zostanie usunięte.", S.importSummary(1, 0))
        assertEquals("Kopia zawiera 22 nowe dni. Nic nie zostanie usunięte.", S.importSummary(22, 0))
        assertEquals("Kopia zawiera 12 nowych dni. Nic nie zostanie usunięte.", S.importSummary(12, 0))
        assertEquals("Kopia zawiera 21 nowych dni. Nic nie zostanie usunięte.", S.importSummary(21, 0))
    }

    @Test
    fun monthsDeclineWhereTheLanguageDoes() {
        S.lang = "ru"
        assertEquals("Самый серый период: с января по март.", S.statsGreyest(1, 3))
        S.lang = "pl"
        assertEquals("Najszarszy okres: od stycznia do marca.", S.statsGreyest(1, 3))
        assertEquals("Najcieplejszy miesiąc: lipiec.", S.statsWarmest(7))
    }

    @Test
    fun turkishDotsItsCapitalI() {
        S.lang = "tr"
        assertEquals("PAZARTESİ", S.caps(S.weekday(sep22.minus(1, DateTimeUnit.DAY))))
        assertEquals("İnci grisi", S.colorName("pearl"))
        S.lang = "en"
        assertEquals("MONDAY", S.caps("Monday"))
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
