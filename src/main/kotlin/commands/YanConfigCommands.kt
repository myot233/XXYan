package com.github.commands

import com.github.XXYan
import com.github.YanConfig
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MessageEvent

object YanConfigCommands : CompositeCommand(
    XXYan,
    "makeYan",
    "SetYan",
    parentPermission = XXYan.permissionAdmin

) {

    @SubCommand("at")
    suspend fun CommandSenderOnMessage<MessageEvent>.byAt(name: String, member: Member) {
        byId(name, member.id)
    }

    @SubCommand("id")
    suspend fun CommandSender.byId(name: String, userId: Long) {
        YanConfig.cares[name] = userId
        this.sendMessage("已成功添加${name} -> $userId")
    }

}