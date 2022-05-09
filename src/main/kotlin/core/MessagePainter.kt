package com.github.core

import com.github.XXYan
import me.gsycl2004.data.Yan
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.*
import javax.imageio.ImageIO
import kotlin.io.path.Path

object MessagePainter {
    private val standardFont: Font =
        Font.createFont(Font.TRUETYPE_FONT, XXYan.getResourceAsStream("msyh.ttc"))

    suspend fun paintMessage(yan: Yan): BufferedImage {
        var image = BufferedImage(880, 230, BufferedImage.TYPE_4BYTE_ABGR)
        fillColor(image)
        drawAvatar(yan, image)
        val length = drawTitle(image, yan)
        val width = drawName(image, yan, length)
        println(width)
        image = extImage(image, newWidth = width)
        var g2d = image.createGraphics()
        g2d.font = standardFont.deriveFont(40f)
        g2d.applyAntialias()
        if (yan.message.contains(net.mamoe.mirai.message.data.Image)) {
            val userImage = downloadAvatar(yan.message[Image]!!.queryUrl())
            println(yan.message[net.mamoe.mirai.message.data.Image.Key]!!.queryUrl())
            image =
                extImage(image, newWidth = 165 * 2 + userImage.width + 10, newHeight = 85 * 2 + userImage.height)
            g2d = image.createGraphics()
            g2d.clip = RoundRectangle2D.Double(
                165.toDouble(),
                85.toDouble(),
                userImage.width.toDouble(),
                userImage.height.toDouble(),
                25.toDouble(),
                25.toDouble()
            )

            g2d.drawImage(userImage, 165, 85, userImage.width, userImage.height, null)
        } else {
            val contentIm = drawContent(yan)
            val bubble = drawBubble(contentIm)
            image =
                extImage(image, 84 + bubble.height + 50, newWidth = listOf(width, bubble.width + 167 + 40).maxOf { it })
            g2d = image.createGraphics()
            g2d.applyAntialias()
            g2d.drawImage(bubble, 167, 84, null)
        }
        return image

    }

    private fun drawBubble(contentIm: BufferedImage): BufferedImage {
        val image = BufferedImage(contentIm.width + 60, contentIm.height + 50, BufferedImage.TYPE_4BYTE_ABGR)
        val g2d = image.createGraphics()
        g2d.fillRoundRect(0, 0, image.width, image.height, 30, 30)
        g2d.applyAntialias()
        g2d.drawImage(contentIm, 30, 13, contentIm.width, contentIm.height, null)
        return image
    }

    private fun drawContent(yan: Yan): BufferedImage {
        val texts = yan.message.joinToString("") { it.contentToString() }.split("\n").toMutableList()
        var image = BufferedImage(680, 60, BufferedImage.TYPE_4BYTE_ABGR)
        var cg2d = image.createGraphics()
        val list = Vector<String>()
        cg2d = image.createGraphics()
        cg2d.font = standardFont.deriveFont(40f)
        fun split(text: String, fontMetrics: FontMetrics, max: Int): List<String> {
            var num = 1
            if (fontMetrics.stringWidth(text.subSequence(0, text.length) as String) <= max) {
                list.add(text)
                return list
            }
            while (true) {
                if (fontMetrics.stringWidth(text.subSequence(0, num) as String) <= max) {
                    num += 1
                } else {
                    num -= 1
                    break
                }
            }
            list.add(text.subSequence(0, num) as String)
            split(text.subSequence(num, text.length) as String, fontMetrics, max)

            return list
        }

        val iterator = texts.iterator()
        val new = ArrayList<String>()
        iterator.forEach { it ->
            println(it)
            new += split(it, fontMetrics = cg2d.fontMetrics, 680)
        }
        image = extImage(image, (cg2d.fontMetrics.height + 5) * new.size, Color(0, 0, 0, 0))
        cg2d = image.createGraphics()
        cg2d.font = standardFont.deriveFont(40f)
        cg2d.color = Color(50, 50, 50)

        new.forEachIndexed { index, s ->
            cg2d.drawString(s, 0, cg2d.fontMetrics.height + index * (cg2d.fontMetrics.height + 5))
        }

        return image.getSubimage(0, 0, new.maxOf { cg2d.fontMetrics.stringWidth(it) }, image.height)
    }


    private fun extImage(
        image: BufferedImage,
        newHeight: Int = image.height,
        backgroundColor: Color = Color(235, 238, 247),
        newWidth: Int = image.width,
    ): BufferedImage {
        if (newHeight < image.height) return image
        if (newWidth < image.height) return image
        val im = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_4BYTE_ABGR)
        fillColor(im, backgroundColor)
        val g2d = im.createGraphics()
        g2d.drawImage(image, 0, 0, image.width, image.height, null)
        return im
    }


    private fun drawName(image: BufferedImage, yan: Yan, length: Int): Int {
        val g2d = image.createGraphics()
        g2d.applyAntialias()
        g2d.color = Color(164, 169, 179)
        g2d.font = standardFont.deriveFont(30f)
        g2d.drawString(yan.sender.name, length + 10, 55)
        return g2d.fontMetrics.stringWidth(yan.sender.name) + length + 10 + 5
    }


    private fun drawTitle(image: BufferedImage, yan: Yan): Int {
        val g2d = image.createGraphics()
        g2d.applyAntialias()
        g2d.font = standardFont.deriveFont(25f).deriveFont(Font.BOLD)
        g2d.color = Color(163, 187, 240)
        val wordLength = g2d.fontMetrics.stringWidth(yan.sender.title)
        g2d.fillRoundRect(170, 25, wordLength + 25, 40, 15, 15)
        g2d.color = Color.WHITE
        g2d.drawString(yan.sender.title, 185, 55)
        return 170 + wordLength + 25
    }

    private fun drawAvatar(yan: Yan, image: BufferedImage) {
        val avatar = downloadAvatar(yan.sender.avatar).circleAvtar()
        val g2d = image.createGraphics()
        g2d.applyAntialias()
        g2d.drawImage(avatar, 25, 20, 110, 110, null)
    }

    private fun downloadAvatar(url: String): BufferedImage {
        return ImageIO.read(URL(url))
    }


    private fun fillColor(image: BufferedImage, color: Color = Color(235, 238, 247)) {
        val g2d = image.createGraphics()
        g2d.color = color
        g2d.fill(Rectangle(0, 0, image.width, image.height))
    }
}

fun Graphics2D.applyAntialias() {
    val renderingHints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    renderingHints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
    this.setRenderingHints(renderingHints)
}

fun Graphics2D.default() {
    this.color
}

fun BufferedImage.testImage() {

    val path = Files.createTempFile(Path("temp\\"), "", ".png")
    ImageIO.write(this, "png", File(path.toString()))
    Runtime.getRuntime().exec("explorer .\\$path")
}

fun BufferedImage.circleAvtar(): BufferedImage {

    var avatarImage: BufferedImage = this
    avatarImage = run {
        val type = avatarImage.colorModel.transparency
        val width = avatarImage.width
        val height = avatarImage.height
        val renderingHints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        renderingHints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
        val img = BufferedImage(avatarImage.width, avatarImage.width, type)
        val graphics2d = img.createGraphics()
        graphics2d.setRenderingHints(renderingHints)
        graphics2d.drawImage(avatarImage, 0, 0, avatarImage.width, avatarImage.width, 0, 0, width, height, null)
        graphics2d.dispose()
        img
    }
    val width = avatarImage.width
    val formatAvatarImage = BufferedImage(width, width, BufferedImage.TYPE_4BYTE_ABGR)
    var graphics = formatAvatarImage.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val border = 0
    val shape = Ellipse2D.Double(
        border.toDouble(), border.toDouble(),
        (width - border * 2).toDouble(), (width - border * 2).toDouble()
    )
    graphics.clip = shape
    graphics.drawImage(avatarImage, border, border, width - border * 2, width - border * 2, null)
    graphics.dispose()
    graphics = formatAvatarImage.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val border1 = 3
    val s: Stroke = BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    graphics.stroke = s
    graphics.color = Color.WHITE

    graphics.dispose()
    return formatAvatarImage
}