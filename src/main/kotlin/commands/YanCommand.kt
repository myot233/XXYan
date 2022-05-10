package com.github.commands

import com.github.XXYan
import com.github.YanConfig
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MessageEvent

object YanCommand : SimpleCommand(
    XXYan,
    "makeYan",
    "SetYan",

) {

    @Handler
    suspend fun CommandSenderOnMessage<MessageEvent>.makeYan(name: String, member: Member) {
        YanConfig.cares[name] = member.id
        fromEvent.subject.sendMessage("已成功添加${name} -> ${member.id}")
    }


}