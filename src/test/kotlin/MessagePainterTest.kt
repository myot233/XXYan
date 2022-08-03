package com.github

import com.github.core.MessagePainter
import com.github.core.data.Sender
import com.github.core.data.ShowYanTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText
import org.junit.Test
import java.io.*
import javax.imageio.ImageIO


class MessagePainterTest {

    @Test
    fun testMultiLines() {
        runBlocking { test(
            """
                foo
                bar
                yan
            """.trimIndent()) }
    }

    @Test
    fun testLongLine() {
        runBlocking { test(
            """
                fooooooooooooooooooooooooooooooooooooooooooooooooEOF
            """.trimIndent()) }
    }

    suspend fun test(text: String) {
        val avatarProvider = {SimpleGeometricImageProvider.apply("矩形 50,50 上下渐变,3,74,144,255,10,151,223,255")}
        val messageChain = MessageChainBuilder()
            .append(PlainText(text))
            .build()
        val image = MessagePainter.paintMessage(
            ShowYanTask(
                Sender(
                    "testName",
                    avatarProvider,
                    1,
                    "testTitle",
                    "red"
                ),
                group = null,
                messageChain
            )
        )

        val byteStream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            ImageIO.write(image, "png", byteStream)
        }
        copyInputStreamToFile(ByteArrayInputStream(byteStream.toByteArray()), File("testOutput.png"))
    }
}

fun copyInputStreamToFile(inputStream: InputStream, file: File?) {
    // append = false
    try {
        FileOutputStream(file, false).use { outputStream ->
            var read: Int
            val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
            while (inputStream.read(bytes).also { read = it } != -1) {
                outputStream.write(bytes, 0, read)
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}