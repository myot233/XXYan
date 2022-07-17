package com.github

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader.startAsDaemon


suspend fun main() {
    startAsDaemon()

    XXYan.load()
    XXYan.enable()

    MiraiConsole.job.join()
}