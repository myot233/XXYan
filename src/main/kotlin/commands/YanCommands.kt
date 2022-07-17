package com.github.commands

import com.github.XXYan
import com.github.YanConfig
import com.github.YanData
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent

object YanCommands : CompositeCommand(
    XXYan,
    "yan",
    parentPermission = XXYan.permission
) {
    @SubCommand
    suspend fun CommandSenderOnMessage<GroupMessageEvent>.length(user: User) {
        val sequence = YanData.getSequence(user.id)
        fromEvent.group.sendMessage("目前该用户的yan数量为:${sequence.rowSet.size()}")
    }

    @SubCommand
    suspend fun CommandSenderOnMessage<GroupMessageEvent>.stars() {
        val sequence = YanConfig.cares
        val same = sequence.values.filter {
            this.fromEvent.group[it] != null
        }.map {
            this.fromEvent.group[it]!!
        }.toSet()
        fromEvent.group.sendMessage("目前关注的人有:\n${same.joinToString("\n") { "${it.nameCardOrNick}(${it.id})" }}")

    }


}

