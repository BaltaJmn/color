package com.baltajmn.color

import com.baltajmn.color.i18n.S
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// What a card or poster signs is the title of each store listing: the one search that finds the app.
class ShareSignatureTest {
    private val listings = generateSequence(File("").absoluteFile) { it.parentFile }
        .map { File(it, "store/listings") }.first { it.isDirectory }

    private val folderOf = mapOf(
        "en" to "en-US", "es" to "es-ES", "pt" to "pt-BR", "de" to "de-DE", "fr" to "fr-FR", "it" to "it-IT",
        "nl" to "nl-NL", "pl" to "pl-PL", "ru" to "ru-RU", "tr" to "tr-TR", "id" to "id", "ja" to "ja-JP", "ko" to "ko-KR",
    )

    @Test
    fun signatureIsTheListingTitleInEveryLanguage() {
        val saved = S.lang
        try {
            folderOf.forEach { (code, folder) ->
                S.lang = code
                assertEquals(File(listings, "$folder/title.txt").readText().trim(), S.shareSignature, code)
            }
        } finally {
            S.lang = saved
        }
    }

    @Test
    fun signatureCarriesTheSearchNotJustTheName() {
        assertTrue(S.shareSignature.length > "Chroma".length)
    }
}
