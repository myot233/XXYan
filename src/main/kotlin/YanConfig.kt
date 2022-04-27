package com.github

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object YanConfig:AutoSavePluginConfig("config") {
    val cares:MutableMap<String,Long> by value(mutableMapOf("1" to 1L))
    val op:MutableList<Long> by value(mutableListOf(123456))
}