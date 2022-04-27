package com.github.core

import com.github.XXYan
import okio.utf8Size
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO


@Suppress("unused")
object Paints {
    private val backGround: BufferedImage
        get() = ImageIO.read(XXYan.getResourceAsStream("background.png")).resize(1080, 267)

    private val font: Font = Font.createFont(Font.TRUETYPE_FONT, XXYan.getResourceAsStream("msyh.ttc")).deriveFont(40f)

    fun paintTextMessage(head: Image, name: String, text: String): BufferedImage {
        val cString = collapseString(text, 15, "\n").split("\n")
        var backgroundImage = backGround.resize(
            1080, 207 + backGround.graphics.getFontMetrics(font).stringsHeight(
                cString
            )
        )
        val resizedHead = head.resize(110, 110)
        var g2d = backgroundImage.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.drawImage(resizedHead.cutHeadImage(), 100, 40, null)
        g2d.font = font
        g2d.color = Color.WHITE
        var num = g2d.getFontMetrics(g2d.font).stringWidth(cString[0]) + 30
        val height = 70 + backGround.graphics.getFontMetrics(font).stringsHeight(cString)
        g2d.fillRoundRect(219, 81, num, height, 50, 50)
        g2d.color = Color(50, 50, 50)
        var lnum = 0
        collapseString(text, 15, "\n").split("\n").forEach {
            g2d.drawString(it, 235, 161 + (backGround.graphics.getFontMetrics(font).height + 5) * lnum)
            lnum += 1
        }
        g2d.font = g2d.font.deriveFont(30f)
        g2d.drawString(name, 235, 65)
        return backgroundImage
    }
}

fun FontMetrics.stringsHeight(strings: List<String>): Int {
    return this.height * strings.size + 5 * strings.size
}

private fun collapseString(target: String, size: Int, insert: String?): String {
    var insert = insert
    if (target.isEmpty()) return target //目标字符串为空，返回目标字符串
    val target_length = target.length
    if (target_length <= size) return target //目标字符串长度等于间隔长度 ， 返回目标字符串
    if (insert != null) {
        insert = insert.ifEmpty { "<br/>" }
    }

    //插入次数
    var times = 0
    times = if (target_length % size == 0) {
        target_length / size - 1
    } else {
        target_length / size
    }

    //结果字符集
    val result_chars = CharArray(target_length + times)

    //目标字符集
    val target_chars = CharArray(target_length)

    //将字符串数据装入目标字符集
    target.toCharArray(target_chars, 0, 0, target_length)

    //遍历目标字符集，将值插入到结果字符集
    var j = 0
    for (i in target_chars.indices) {

        //间隔处插入值
        if (i > 0 && i % size == 0) {
            result_chars[j] = '`'
            j = j + 1
        }
        result_chars[j] = target_chars[i]
        j = j + 1
    }
    val resultStr = String(result_chars)
    return resultStr.replace("`".toRegex(), insert!!)
}

@Throws(IOException::class)
fun Image.resize(width: Int, height: Int): BufferedImage {
    val imageBuffer = this
    val tempBuffer = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    val graphics: Graphics = tempBuffer.createGraphics()
    graphics.drawImage(imageBuffer, 0, 0, width, height, null)
    imageBuffer.flush()
    tempBuffer.flush()
    return tempBuffer
}

fun BufferedImage.cutHeadImage(): BufferedImage {

    var avatarImage: BufferedImage = this
    avatarImage = scaleByPercentage(avatarImage, avatarImage.width, avatarImage.width)
    val width = avatarImage.width
    // 透明底的图片
    val formatAvatarImage = BufferedImage(width, width, BufferedImage.TYPE_4BYTE_ABGR)
    var graphics = formatAvatarImage.createGraphics()
    //把图片切成一个园
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    //留一个像素的空白区域，这个很重要，画圆的时候把这个覆盖
    val border = 0
    //图片是一个圆型
    val shape = Ellipse2D.Double(
        border.toDouble(), border.toDouble(),
        (width - border * 2).toDouble(), (width - border * 2).toDouble()
    )
    //需要保留的区域
    graphics.clip = shape
    graphics.drawImage(avatarImage, border, border, width - border * 2, width - border * 2, null)
    graphics.dispose()
    //在圆图外面再画一个圆
    //新创建一个graphics，这样画的圆不会有锯齿
    graphics = formatAvatarImage.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val border1 = 3
    //画笔是4.5个像素，BasicStroke的使用可以查看下面的参考文档
    //使画笔时基本会像外延伸一定像素，具体可以自己使用的时候测试
    val s: Stroke = BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    graphics.stroke = s
    graphics.color = Color.WHITE

    graphics.dispose()
    return formatAvatarImage
}

/**
 * 缩小Image，此方法返回源图像按给定宽度、高度限制下缩放后的图像
 *
 * @param inputImage
 * ：压缩后宽度
 * ：压缩后高度
 * @throws java.io.IOException
 * return
 */
fun scaleByPercentage(inputImage: BufferedImage, newWidth: Int, newHeight: Int): BufferedImage {
    // 获取原始图像透明度类型
    val type = inputImage.colorModel.transparency
    val width = inputImage.width
    val height = inputImage.height
    // 开启抗锯齿
    val renderingHints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    // 使用高质量压缩
    renderingHints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
    val img = BufferedImage(newWidth, newHeight, type)
    val graphics2d = img.createGraphics()
    graphics2d.setRenderingHints(renderingHints)
    graphics2d.drawImage(inputImage, 0, 0, newWidth, newHeight, 0, 0, width, height, null)
    graphics2d.dispose()
    return img
}