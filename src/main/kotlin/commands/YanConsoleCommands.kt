package com.github.commands

import com.github.XXYan
import com.github.YanConfig
import com.github.YanData
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender

object YanConsoleCommands : CompositeCommand(
    XXYan,
    "yanConsole",
    parentPermission = XXYan.permission
) {
    @SubCommand
    suspend fun ConsoleCommandSender.length(userId: Long) {
        val sequence = YanData.getSequence(userId)
        this.sendMessage("目前该用户的yan数量为:${sequence.rowSet.size()}")
    }

    @SubCommand
    suspend fun ConsoleCommandSender.unsetYan(name: String) {
        val value = YanConfig.cares.remove(name)
        this.sendMessage("已成功移除${name} -> $value")
    }

    @SubCommand
    suspend fun ConsoleCommandSender.stars() {
        val sequence = YanConfig.cares
        val ids = sequence.values.toSet()
        this.sendMessage("目前关注的人有:\n${ids.joinToString("\n")}")
    }


}

