package com.baltajmn.color.data

import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private const val JPEG_QUALITY = 0.85

actual object Capture {

    actual val cameraAvailable: Boolean
        get() = UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
        )

    // The pickers hold their delegates weakly, so the one in flight is kept alive here.
    private var held: NSObject? = null

    private val root: UIViewController?
        get() = UIApplication.sharedApplication.keyWindow?.rootViewController?.let { top(it) }

    private fun top(vc: UIViewController): UIViewController = vc.presentedViewController?.let(::top) ?: vc

    /** A photo just taken is today's by definition, so it carries no date to check. */
    actual suspend fun camera(): Picked? = suspendCancellableCoroutine { cont ->
        val presenter = root
        if (presenter == null || !cameraAvailable) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        val controller = UIImagePickerController()
        controller.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        val delegate = CameraDelegate { image ->
            held = null
            cont.resume(image?.toStoredJpeg()?.let { Picked(it, null) })
        }
        held = delegate
        controller.delegate = delegate
        presenter.presentViewController(controller, true, null)
    }

    /** PHPicker needs no photo-library permission: the user hands over one image and nothing else. */
    actual suspend fun gallery(): Picked? = suspendCancellableCoroutine { cont ->
        val presenter = root
        if (presenter == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        val config = PHPickerConfiguration().apply {
            setFilter(PHPickerFilter.imagesFilter())
            setSelectionLimit(1)
        }
        val controller = PHPickerViewController(configuration = config)
        val delegate = GalleryDelegate { data ->
            held = null
            val picked = data?.let { d ->
                UIImage.imageWithData(d)?.toStoredJpeg()?.let { Picked(it, parseExifDate(exifDate(d))) }
            }
            cont.resume(picked)
        }
        held = delegate
        controller.delegate = delegate
        presenter.presentViewController(controller, true, null)
    }

    private class CameraDelegate(
        private val done: (UIImage?) -> Unit,
    ) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

        override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
            picker.dismissViewControllerAnimated(true, null)
            done(didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage)
        }

        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            picker.dismissViewControllerAnimated(true, null)
            done(null)
        }
    }

    private class GalleryDelegate(
        private val done: (NSData?) -> Unit,
    ) : NSObject(), PHPickerViewControllerDelegateProtocol {

        override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
            picker.dismissViewControllerAnimated(true, null)
            val provider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider
            if (provider == null) return finish(null)
            // The raw data, not a UIImage: the EXIF date lives in the bytes.
            provider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ -> finish(data) }
        }

        /** The item provider calls back off the main thread; Compose state must not be touched there. */
        private fun finish(data: NSData?) {
            dispatch_async(dispatch_get_main_queue()) { done(data) }
        }
    }
}

/** EXIF DateTimeOriginal, read by ImageIO without decoding the image. */
@OptIn(ExperimentalForeignApi::class)
private fun exifDate(data: NSData): String? {
    @Suppress("UNCHECKED_CAST")
    val cfData = CFBridgingRetain(data) as CFDataRef?
    val source = CGImageSourceCreateWithData(cfData, null)
    val props = source?.let { CGImageSourceCopyPropertiesAtIndex(it, 0u, null) }
    val dict = props?.let { CFBridgingRelease(it) } as? Map<*, *>
    source?.let { CFRelease(it) }
    cfData?.let { CFRelease(it) }
    val exif = dict?.get("{Exif}") as? Map<*, *>
    val tiff = dict?.get("{TIFF}") as? Map<*, *>
    return (exif?.get("DateTimeOriginal") ?: tiff?.get("DateTime")) as? String
}

/**
 * Redrawn at [PHOTO_SIDE] on the long side. Drawing applies the orientation, and the new JPEG
 * carries none of the original metadata, GPS included.
 */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toStoredJpeg(): ByteArray? = toJpeg(PHOTO_SIDE, JPEG_QUALITY)

private fun UIImage.toJpeg(side: Int, quality: Double): ByteArray? {
    val width = size.useContents { this.width }
    val height = size.useContents { this.height }
    if (width <= 0.0 || height <= 0.0) return null
    val scale = (side.toDouble() / maxOf(width, height)).coerceAtMost(1.0)
    val w = width * scale
    val h = height * scale
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply { setScale(1.0) }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(w, h), format = format)
    val scaled = renderer.imageWithActions { this@toJpeg.drawInRect(CGRectMake(0.0, 0.0, w, h)) }
    return UIImageJPEGRepresentation(scaled, quality)?.toByteArray()
}

actual fun reencodeJpeg(jpeg: ByteArray, side: Int, quality: Int): ByteArray? {
    if (jpeg.isEmpty()) return null
    val data = jpeg.usePinned { NSData.create(bytes = it.addressOf(0), length = jpeg.size.toULong()) }
    return UIImage.imageWithData(data)?.toJpeg(side, quality / 100.0)
}
