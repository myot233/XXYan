package com.github

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object YanConfig:AutoSavePluginConfig("config") {
    @ValueDescription("一个用于指定指令对应触发的列表")
    val cares:MutableMap<String,Long> by value(mutableMapOf("1" to 1L))
    @ValueDescription("这里可以指定字体")
    val font by value("sarasa-ui-tc-regular.ttf")
}