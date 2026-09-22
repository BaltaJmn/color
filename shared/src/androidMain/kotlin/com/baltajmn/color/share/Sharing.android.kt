package com.baltajmn.color.share

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import com.baltajmn.color.data.AndroidContext
import java.io.ByteArrayOutputStream
import java.io.File

actual fun ImageBitmap.encodeToPng(): ByteArray {
    val out = ByteArrayOutputStream()
    asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
    return out.toByteArray()
}

actual object Sharing {

    actual fun sharePng(png: ByteArray) {
        val context = AndroidContext.value
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        // One name, always: the card of a moment ago has nothing to say once it is shared.
        val file = File(dir, "chroma.png").apply { writeBytes(png) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Scoped storage asks for nothing; below Q it would need a permission, so the row is hidden. */
    actual val canSaveToPhotos: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    actual fun savePngToPhotos(png: ByteArray, onResult: (Boolean) -> Unit) {
        if (!canSaveToPhotos) return onResult(false)
        val resolver = AndroidContext.value.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "chroma-${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Chroma")
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri == null) return onResult(false)
        onResult(
            runCatching { resolver.openOutputStream(uri)?.use { it.write(png) } ?: error("no stream") }.isSuccess,
        )
    }
}
