package com.baltajmn.color.data

import androidx.compose.ui.graphics.ImageBitmap

/** Decode a JPEG that came from the camera, the gallery or photos/. */
expect fun decodeImage(bytes: ByteArray): ImageBitmap?

/** Decoded photos, kept in memory so a day that is drawn twice is not read from disk twice. */
object Photos {
    private val cache = mutableMapOf<String, ImageBitmap?>()

    fun get(name: String?): ImageBitmap? {
        if (name == null) return null
        // containsKey, not getOrPut: a photo whose file went missing has to be remembered as
        // missing, or every frame goes back to the disk looking for it.
        if (cache.containsKey(name)) return cache[name]
        val decoded = Storage.readPhoto(name)?.let(::decodeImage)
        cache[name] = decoded
        return decoded
    }

    fun forget(name: String) {
        cache.remove(name)
    }
}

/** A photo name that no entry and no file uses yet. A new name per photo keeps stale caches out. */
fun freePhotoName(taken: Set<String>): String {
    while (true) {
        val name = "p-" + kotlin.random.Random.nextInt().toUInt().toString(16).padStart(8, '0') + ".jpg"
        if (name !in taken) return name
    }
}

/**
 * A photo name is a plain file name inside photos/ and nothing else. A hand written backup saying
 * "../entries.json" would otherwise walk out of the folder and take the journal with it.
 */
fun isSafePhotoName(name: String): Boolean =
    name.isNotEmpty() && '/' !in name && '\\' !in name && name != "." && name != ".."
