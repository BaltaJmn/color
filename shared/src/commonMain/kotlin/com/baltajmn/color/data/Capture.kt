package com.baltajmn.color.data

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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

    /** Null when the user cancels or the photo cannot be read. */
    suspend fun camera(): Picked?

    suspend fun gallery(): Picked?
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
 * The photo squeezed into [SAMPLE_SIDE] x [SAMPLE_SIDE] ARGB pixels for the extraction. Squeezing
 * changes the shape but not how much of the picture each color covers, which is all that counts.
 */
fun samplePixels(jpeg: ByteArray): IntArray? {
    val image = decodeImage(jpeg) ?: return null
    return samplePixels(image)
}

fun samplePixels(image: ImageBitmap): IntArray {
    val target = ImageBitmap(SAMPLE_SIDE, SAMPLE_SIDE)
    Canvas(target).drawImageRect(
        image,
        IntOffset.Zero,
        IntSize(image.width, image.height),
        IntOffset.Zero,
        IntSize(SAMPLE_SIDE, SAMPLE_SIDE),
        Paint().apply { filterQuality = androidx.compose.ui.graphics.FilterQuality.Low },
    )
    val pixels = IntArray(SAMPLE_SIDE * SAMPLE_SIDE)
    target.readPixels(pixels)
    return pixels
}

