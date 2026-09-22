package com.baltajmn.color.data

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

private const val JPEG_QUALITY = 85

actual object Capture {

    /** Set by MainActivity: the system screens need an Activity to launch from. */
    var launchCamera: ((Uri) -> Unit)? = null
    var launchGallery: (() -> Unit)? = null

    // The answer comes back to the Activity, which may be a new instance by then, so what is waiting
    // for it lives here.
    private var waiting: ((Uri?) -> Unit)? = null

    actual val cameraAvailable: Boolean
        get() = AndroidContext.value.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    private val shot: File
        get() = File(AndroidContext.value.cacheDir, "capture").apply { mkdirs() }.resolve("shot.jpg")

    actual suspend fun camera(): Picked? {
        val launch = launchCamera ?: return null
        val file = shot.apply { delete() }
        val uri = FileProvider.getUriForFile(AndroidContext.value, "${AndroidContext.value.packageName}.fileprovider", file)
        val answer = await { launch(uri) } ?: return null
        return withContext(Dispatchers.IO) {
            runCatching { process { AndroidContext.value.contentResolver.openInputStream(answer) } }.getOrNull()
                .also { file.delete() }
        }
    }

    actual suspend fun gallery(): Picked? {
        val launch = launchGallery ?: return null
        val answer = await(launch) ?: return null
        return withContext(Dispatchers.IO) {
            runCatching { process { AndroidContext.value.contentResolver.openInputStream(answer) } }.getOrNull()
        }
    }

    /** Called by MainActivity when the camera answers: the photo is at the uri it was given. */
    fun onCamera(ok: Boolean) {
        val file = shot
        deliver(if (ok && file.exists()) Uri.fromFile(file) else null)
    }

    /** Called by MainActivity when the photo picker answers. */
    fun onGallery(uri: Uri?) = deliver(uri)

    private fun deliver(uri: Uri?) {
        val answer = waiting ?: return
        waiting = null
        answer(uri)
    }

    private suspend fun await(launch: () -> Unit): Uri? = suspendCancellableCoroutine { cont ->
        // One capture at a time: a second one would leave the first with nobody listening.
        if (waiting != null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        waiting = { cont.resume(it) }
        cont.invokeOnCancellation { waiting = null }
        launch()
    }
}

/**
 * Orients, scales to [PHOTO_SIDE] and re-encodes. The EXIF block does not survive the re-encode,
 * which is the point: the location of a photo never reaches photos/.
 */
internal fun process(open: () -> InputStream?): Picked? {
    val exif = open()?.use { ExifInterface(it) }
    val takenOn = parseExifDate(exif?.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))
        ?: parseExifDate(exif?.getAttribute(ExifInterface.TAG_DATETIME))
    val degrees = when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        // ponytail: mirrored orientations (front cameras on a few phones) are drawn unmirrored.
        else -> 0f
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    open()?.use { BitmapFactory.decodeStream(it, null, bounds) }
    val longSide = max(bounds.outWidth, bounds.outHeight)
    if (longSide <= 0) return null
    var sample = 1
    while (longSide / sample > PHOTO_SIDE * 2) sample *= 2
    val decoded = open()?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: return null

    val scale = (PHOTO_SIDE.toFloat() / max(decoded.width, decoded.height)).coerceAtMost(1f)
    val matrix = Matrix().apply {
        postScale(scale, scale)
        postRotate(degrees)
    }
    val bitmap = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    val jpeg = ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        out.toByteArray()
    }
    return Picked(jpeg, takenOn)
}

actual fun reencodeJpeg(jpeg: ByteArray, side: Int, quality: Int): ByteArray? {
    val decoded = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size) ?: return null
    val scale = (side.toFloat() / max(decoded.width, decoded.height)).coerceAtMost(1f)
    val bitmap = Bitmap.createScaledBitmap(
        decoded,
        (decoded.width * scale).toInt().coerceAtLeast(1),
        (decoded.height * scale).toInt().coerceAtLeast(1),
        true,
    )
    return ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        out.toByteArray()
    }
}
