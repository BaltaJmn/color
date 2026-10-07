package com.baltajmn.color.data

import androidx.compose.ui.graphics.ImageBitmap
import com.baltajmn.color.color.sampleOf
import com.baltajmn.color.model.logicalDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** Longest side of a stored photo: the share card is 1080 wide. */
const val PHOTO_SIDE = 1080

/** Side of the thumbnail the colors are extracted from. */
const val SAMPLE_SIDE = 64

/**
 * A photo handed over by the system: already oriented, scaled to [PHOTO_SIDE] and re-encoded, so
 * its metadata (GPS included) is gone before it is ever stored. [takenOn] is what EXIF said, if it
 * said anything.
 */
class Picked(val jpeg: ByteArray, val takenOn: LocalDateTime?)

/** The system camera and the system photo picker. Neither needs a permission of its own. */
/**
 * Decodes, scales so the long side is at most [side], and encodes again at [quality] (0 to 100).
 * Whatever metadata the input carried does not survive. Null if [jpeg] is not an image.
 */
expect fun reencodeJpeg(jpeg: ByteArray, side: Int, quality: Int): ByteArray?

expect object Capture {
    /** False on a device without a camera, like the iOS simulator. */
    val cameraAvailable: Boolean

    /** True when the user said no to the camera (iOS): the camera would only open black. */
    val cameraDenied: Boolean

    /** The last [camera] or [gallery] found nothing on the phone to open (Android). */
    val launchFailed: Boolean

    /**
     * Null when the user cancels. A photo that came back but cannot be read is null on iOS and an
     * empty [Picked.jpeg] on Android, which Today reports as unreadable.
     */
    suspend fun camera(): Picked?

    suspend fun gallery(): Picked?

    /**
     * A photo the camera or the picker handed back while nobody was waiting for it: the process
     * died behind them, or the Activity was rebuilt. Fresh ones only, and only once. Null on iOS,
     * where both live inside the app.
     */
    suspend fun leftover(): Picked?
}

/**
 * A gallery photo dated another day is not today's color. One with no readable date (a screenshot,
 * a photo that went through a messenger) is accepted: with nothing to win, nobody is cheating, and
 * refusing it would punish the honest.
 */
fun isFromToday(takenOn: LocalDateTime?, today: LocalDate): Boolean =
    takenOn == null || logicalDate(takenOn) == today

/** EXIF writes "2026:09:22 18:04:11". Anything else is no date. */
fun parseExifDate(s: String?): LocalDateTime? {
    val m = s?.trim()?.let { Regex("""(\d{4}):(\d{2}):(\d{2}) (\d{2}):(\d{2}):(\d{2})""").matchEntire(it) } ?: return null
    val (y, mo, d, h, mi, se) = m.destructured
    return runCatching { LocalDateTime(y.toInt(), mo.toInt(), d.toInt(), h.toInt(), mi.toInt(), se.toInt()) }.getOrNull()
}

/**
 * The [SAMPLE_SIDE] x [SAMPLE_SIDE] ARGB pixels the colors come from: the visible 4:5 of the photo,
 * averaged in common code ([sampleOf]) so both platforms count the same pixels the same way.
 */
fun samplePixels(image: ImageBitmap): IntArray {
    val pixels = IntArray(image.width * image.height)
    image.readPixels(pixels)
    return sampleOf(pixels, image.width, image.height, SAMPLE_SIDE)
}
