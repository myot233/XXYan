package com.github

import com.github.commands.YanConfigCommands
import com.github.commands.YanQueryCommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.github.core.MessagePainter
import com.github.core.data.Sender
import com.github.core.data.ShowYanTask
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.ktorm.entity.add
import org.ktorm.entity.toList
import java.io.ByteArrayOutputStream
import java.util.concurrent.ThreadLocalRandom
import javax.imageio.ImageIO
import kotlin.random.asKotlinRandom
import kotlin.streams.toList


object XXYan : KotlinPlugin(JvmPluginDescription(
    id = "com.github.XXYan",
    name = "XXYan",
    version = "0.0.4",
) {
    author("gsycl2004")
    info("""to record someone's word""")
}) {
    private val notFoundImage by lazy {
        val inputStream = getResourceAsStream("notFoundImage.jpg")
        if (inputStream != null) {
            ImageIO.read(inputStream)
        } else {
            throw UnsupportedOperationException("notFoundImage初始化失败")
        }
    }

    val permission by lazy {
        PermissionService.INSTANCE.register(PermissionId("yan", "command"), "yans", permissionAll)
    }

    val permissionAdmin by lazy {
        PermissionService.INSTANCE.register(PermissionId("yan", "su"), "yans", permissionAll)
    }

    private val permissionAll by lazy {
        PermissionService.INSTANCE.register(PermissionId("yan", "*"), "yans")
    }

    private fun String.judgeRegex(name: String): Boolean {
        val regex = "^$name *(.*)".toRegex()
        return regex.containsMatchIn(this)
    }

    private fun String.matchList(name: String): List<String> {
        val regex = "($name) *(.*)".toRegex()
        return regex.find(this)!!.groupValues.toList()
    }

    override fun onEnable() {
        Class.forName("org.sqlite.JDBC")
        YanConfigCommands.register()
        YanQueryCommands.register()
        YanConfig.reload()
        globalEventChannel().subscribe<GroupMessageEvent> { it ->
            if (YanConfig.cares.keys.firstOrNull { this.message.contentToString().judgeRegex(it)} != null) {
                it.handleAsShowYan()
            } else if (sender.id in YanConfig.cares.values && this.message.contentToString() != "") {
                it.handleAsHistory()
            }
            return@subscribe ListeningStatus.LISTENING
        }

    }

    private fun GroupMessageEvent.handleAsHistory() {
        val drawText = message.serializeToDrawText(group)
        if (drawText != "") {
            val sequence = YanData.getSequence(sender.id)
            sequence.add(YanEntity {
                name = senderName
                head = sender.avatarUrl
                this.yan = message.serializeToMiraiCode()
                this.yanCode = message.serializeToYanCode()
                this.title = if (sender.specialTitle != "") sender.specialTitle else YanConfig.defaultTitle
            })
        }
    }

    private suspend fun GroupMessageEvent.handleAsShowYan() {
        println("${this.sender.nameCardOrNick} from ${this.group.name} has requested")
        val args = this.message.contentToString()
            .matchList(YanConfig.cares.keys.first { this.message.contentToString().judgeRegex(it) })
        val searchId = args[1];
        val searchYanCode = if (args.size < 3) {
            null
        } else {
            args[2].lowercase()
        }
        val yanList = YanData.getSequence(YanConfig.cares[searchId]!!).toList()
        val yan: YanEntity? = if (searchYanCode == null) {
            yanList.random(ThreadLocalRandom.current().asKotlinRandom())
        } else yanList.filter {
            //it.yanCode != "" &&
            it.yanCode.lowercase().contains(searchYanCode)

        }.randomOrNull(ThreadLocalRandom.current().asKotlinRandom())
        val showYanTask = if (yan != null) {
            val chain = yan.yan.deserializeMiraiCode()
            ShowYanTask(
                Sender(
                    YanConfig.NameMap[YanConfig.cares[searchId]] ?: yan.name,
                    { MessagePainter.downloadAvatar(yan.head) },
                    1,
                    yan.title,
                    "red"
                ),
                group,
                chain
            )
        } else {
            ShowYanTask(
                Sender(
                    "错误警告",
                    { notFoundImage!! },
                    1,
                    "警告",
                    "red"
                ),
                group,
                messageChainOf(PlainText(YanConfig.missText))
            )

        }


        try {
            val image = MessagePainter.paintMessage(
                showYanTask
            )

            val byteStream = ByteArrayOutputStream()
            withContext(Dispatchers.IO) {
                ImageIO.write(image, "png", byteStream)
            }
            byteStream.toByteArray().toExternalResource("png").use {
                val miraiImage = group.uploadImage(it)
                this.group.sendMessage(miraiImage)
            }
        } catch (ex: Exception) {
            logger.error(ex)
            this.group.sendMessage(YanConfig.failedText)
        }
    }

    /**
     * DrawText: 为绘制图片设计的格式，某些SingleMessage类型会被忽略
     */
    fun MessageChain.serializeToDrawText(group: Group?): String {
        val text = this.joinToString("") {
            when (it) {
                is At -> it.getDisplay(group)
                is PlainText -> it.contentToString()
                else -> ""
            }
        }
        return text
    }

    /**
     * YanCode: 为yan检索设计的格式，某些SingleMessage类型会被忽略
     */
    fun MessageChain.serializeToYanCode(): String {
        return this.stream()
            .map {
                when (it) {
                    is PlainText -> it.contentToString()
                    is Image -> it.contentToString()
                    is Face -> it.contentToString()
                    else -> ""
                }
            }
            .toList()
            .joinToString()
    }
}
