package com.baltajmn.color.share

import androidx.compose.ui.graphics.ImageBitmap

/** PNG bytes of the card, exactly the bitmap the preview showed. */
expect fun ImageBitmap.encodeToPng(): ByteArray

expect object Sharing {
    /** Opens the system share sheet with the image attached. */
    fun sharePng(png: ByteArray)

    /** The share sheet with plain text: the invite link. */
    fun shareText(text: String)

    /** False where the system would need a storage permission for it (Android 9 and older). */
    val canSaveToPhotos: Boolean

    /** Writes the image into the photo library. */
    fun savePngToPhotos(png: ByteArray, onResult: (Boolean) -> Unit)
}
