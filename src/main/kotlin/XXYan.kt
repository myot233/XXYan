package com.github

import com.github.commands.YanConsoleCommands
import com.github.commands.YanCommand
import com.github.commands.YanCommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.github.core.MessagePainter
import com.github.core.data.Sender
import com.github.core.data.Yan
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.ktorm.entity.add
import org.ktorm.entity.toList
import java.io.ByteArrayOutputStream
import java.util.concurrent.ThreadLocalRandom
import javax.imageio.ImageIO
import kotlin.random.asKotlinRandom


object XXYan : KotlinPlugin(JvmPluginDescription(
    id = "com.github.XXYan",
    name = "XXYan",
    version = "0.0.4",
) {
    author("gsycl2004")
    info("""to record someone's word""")
}) {
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
        YanCommands.register()
        Class.forName("org.sqlite.JDBC")
        YanCommand.register()
        YanConsoleCommands.register()
        YanConfig.reload()
        globalEventChannel().subscribe<GroupMessageEvent> {
            if (sender.id in YanConfig.cares.values && this.message.contentToString() != "") {
                val yan = YanData.getSequence(sender.id)
                yan.add(YanEntity {
                    name = senderName
                    head = sender.avatarUrl
                    this.yan = message.serializeToMiraiCode()
                    this.title = if (sender.specialTitle != "") sender.specialTitle else YanConfig.defaultTitle
                })
            }
            return@subscribe ListeningStatus.LISTENING
        }
        globalEventChannel().subscribe<GroupMessageEvent> {
            if (YanConfig.cares.keys.firstOrNull { this.message.contentToString().judgeRegex(it) } != null) {
                val args = this.message.contentToString()
                    .matchList(YanConfig.cares.keys.first { this.message.contentToString().judgeRegex(it) })
                val pc = YanData.getSequence(YanConfig.cares[args[1]]!!)
                val yan: YanEntity = if (args.size < 3) {

                    pc.toList().random(ThreadLocalRandom.current().asKotlinRandom())
                } else pc.toList().filter {
                    it.yan.lowercase().contains(args[2].lowercase())
                }.randomOrNull(ThreadLocalRandom.current().asKotlinRandom()) ?: YanEntity {
                    this.name = "错误警告"
                    this.head = "https://bpic.588ku.com/element_origin_min_pic/19/04/09/c1c737167e3c4e03d61ff71d043df148.jpg"
                    this.yan = YanConfig.missText
                    this.title = "警告"
                }
                val chain = yan.yan.deserializeMiraiCode()
                try {
                    val image = MessagePainter.paintMessage(
                        Yan(
                            Sender(
                                YanConfig.NameMap[YanConfig.cares[args[1]]] ?: yan.name, yan.head, 1,yan.title, "red"
                            ),
                            chain
                        )
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
            return@subscribe ListeningStatus.LISTENING
        }


    }


}
