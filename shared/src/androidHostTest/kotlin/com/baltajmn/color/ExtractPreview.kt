package com.baltajmn.color

import com.baltajmn.color.color.extractSwatches
import com.baltajmn.color.color.rgbOf
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

/**
 * Not a test: a contact sheet of what real photos offer, to judge the extraction by eye (#8).
 * Runs only with CHROMA_PHOTOS pointing at a folder of images; writes build/extract-preview.png.
 */
class ExtractPreview {
    @Test
    fun sheet() {
        val dir = System.getenv("CHROMA_PHOTOS")?.let(::File) ?: return
        val photos = dir.listFiles().orEmpty().filter { it.extension.lowercase() in setOf("png", "jpg", "jpeg") }.sorted()
        val row = 96
        val sheet = BufferedImage(row + 5 * row, row * photos.size, BufferedImage.TYPE_INT_RGB)
        val g = sheet.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        photos.forEachIndexed { i, f ->
            val img = ImageIO.read(f) ?: return@forEachIndexed
            val sample = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
            sample.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                drawImage(img, 0, 0, 64, 64, null)
                dispose()
            }
            val swatches = extractSwatches(sample.getRGB(0, 0, 64, 64, null, 0, 64))
            g.drawImage(img, 0, i * row, row, row, null)
            swatches.forEachIndexed { j, s ->
                g.color = Color(rgbOf(s.color))
                g.fillRect(row + j * row, i * row, row, row)
            }
        }
        g.dispose()
        ImageIO.write(sheet, "png", File("build/extract-preview.png"))
    }
}
