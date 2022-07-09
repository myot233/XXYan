package com.github

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object YanConfig:AutoSavePluginConfig("config") {
    @ValueDescription("默认称号")
    val defaultTitle:String by value("小萝莉")
    @ValueDescription("这里用来指定搜索不到的内容")
    val missText by value("搜索失败,请重试")
    @ValueDescription("这里用来指定搜索失败后回复的内容")
    val failedText by value("遇到未知错误,生成yan失败")
    @ValueDescription("给某个用户指定一个固定昵称")
    val NameMap:MutableMap<Long,String> by value()
    @ValueDescription("这里可以指定字体")
    val font by value("sarasa-ui-tc-regular.ttf")
    @ValueDescription("一个用于指定指令对应触发的列表")
    val cares:MutableMap<String,Long> by value(mutableMapOf("样例yan" to 1234567L))

}