package com.baltajmn.color.data

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
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
private const val LEFTOVER_MS = 15 * 60_000L

actual object Capture {

    /** Set by MainActivity: the system screens need an Activity to launch from. */
    var launchCamera: ((Uri) -> Unit)? = null
    var launchGallery: (() -> Unit)? = null

    // The answer comes back to the Activity, and what is waiting for it lives here. The wait belongs
    // to Today's composition, so a process killed behind the camera (or an Activity rebuilt, say by
    // a language change) finds nobody waiting: shot.jpg stays, and leftover() hands it to Today on
    // its return to the front instead of losing the photo of the moment.
    private var waiting: ((Uri?) -> Unit)? = null
    private var shooting = false

    // The gallery's shot.jpg. Up to Android 11 the picker is the full-screen documents app, and
    // Chroma behind it can be killed like behind the camera.
    private var orphan: Uri? = null

    // A camera is not enough: a work profile or a disabled camera app leaves nobody to answer the
    // intent. Seeing that answer needs the <queries> entry in the manifest. Asked once per process,
    // and a launch that fails anyway turns the button off.
    private val cameraAnswers by lazy {
        AndroidContext.value.packageManager.let {
            it.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) &&
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(it) != null
        }
    }
    private var cameraFailed = false

    actual val cameraAvailable: Boolean get() = cameraAnswers && !cameraFailed

    private var failed = false
    actual val launchFailed: Boolean get() = failed

    // The system camera app takes the photo, so Chroma itself never holds the permission.
    actual val cameraDenied: Boolean = false

    // Not cacheDir: low on space, the system empties it while the camera is open and the photo is lost.
    private val shot: File
        get() = File(AndroidContext.value.filesDir, "capture").apply { mkdirs() }.resolve("shot.jpg")

    actual suspend fun camera(): Picked? {
        val launch = launchCamera ?: return null
        val file = shot.apply { delete() }
        val uri = FileProvider.getUriForFile(AndroidContext.value, "${AndroidContext.value.packageName}.fileprovider", file)
        shooting = true
        try {
            val answer = await { launch(uri) }
            if (launchFailed) cameraFailed = true
            if (answer == null) {
                // A cancel, not the process dying behind the camera: some cameras leave the photo
                // anyway, and leftover() must not bring back what the user threw away.
                file.delete()
                return null
            }
            return read(answer).also { file.delete() }
        } finally {
            shooting = false
        }
    }

    actual suspend fun leftover(): Picked? {
        if (shooting || waiting != null) return null
        // Asked on every return to the front: a launch that failed long ago is not news again.
        failed = false
        orphan?.let {
            orphan = null
            return read(it)
        }
        val file = shot
        if (!file.exists()) return null
        // An old one is a photo the user already gave up on.
        val fresh = file.length() > 0 && System.currentTimeMillis() - file.lastModified() in 0..LEFTOVER_MS
        return (if (fresh) read(Uri.fromFile(file)) else null).also { file.delete() }
    }

    actual suspend fun gallery(): Picked? {
        val launch = launchGallery ?: return null
        val answer = await(launch) ?: return null
        return read(answer)
    }

    // Empty, not null: a photo that came back unreadable is said so, not taken for a cancel.
    private suspend fun read(uri: Uri): Picked = withContext(Dispatchers.IO) {
        runCatching { process { AndroidContext.value.contentResolver.openInputStream(uri) } }.getOrNull()
            ?: Picked(ByteArray(0), null)
    }

    /** Called by MainActivity when the camera answers: the photo is at the uri it was given. */
    fun onCamera(ok: Boolean) {
        val file = shot
        deliver(if (ok && file.exists()) Uri.fromFile(file) else null)
    }

    /** Called by MainActivity when the photo picker answers. */
    fun onGallery(uri: Uri?) {
        if (waiting == null) orphan = uri else deliver(uri)
    }

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
        failed = false
        // Nobody to answer the intent throws here, and the throw would close the app.
        runCatching(launch).onFailure {
            failed = true
            waiting = null
            cont.resume(null)
        }
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
    // The whole EXIF table: the mirrored ones (front cameras on some phones) also turn.
    val orient = Matrix().apply {
        when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(-90f); postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
            else -> Unit
        }
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
        postConcat(orient)
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
