package com.github

import net.mamoe.mirai.console.command.BuiltInCommands.AutoLoginCommand.add
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MessageEvent

object YanCommand:SimpleCommand(
    XXYan,
    "yanconf"

) {

    @Handler
    suspend fun CommandSenderOnMessage<MessageEvent>.yanconf(name:String,member:Member){
        YanConfig.cares[name] = member.id
        fromEvent.subject.sendMessage("已成功添加${name} -> ${member.id}")

    }



}