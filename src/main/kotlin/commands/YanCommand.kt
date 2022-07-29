package com.github.commands

import com.github.XXYan
import com.github.YanConfig
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MessageEvent

object YanCommand : SimpleCommand(
    XXYan,
    "makeYan",
    "SetYan",
    parentPermission = XXYan.permissionAdmin

) {

    @Handler
    suspend fun CommandSenderOnMessage<MessageEvent>.makeYan(name: String, member: Member) {
        makeYan(name, member.id)
    }

    @Handler
    suspend fun CommandSender.makeYan(name: String, userId: Long) {
        YanConfig.cares[name] = userId
        this.sendMessage("已成功添加${name} -> $userId")
    }

}