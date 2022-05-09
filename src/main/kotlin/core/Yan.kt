package me.gsycl2004.data

import net.mamoe.mirai.message.data.MessageChain

@kotlinx.serialization.Serializable
data class Yan(
    val sender: Sender,
    val message: MessageChain
    )