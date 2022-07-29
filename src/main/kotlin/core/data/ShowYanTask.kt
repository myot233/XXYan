package com.github.core.data

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MessageChain


data class ShowYanTask(
    val sender: Sender,
    val group: Group?,
    val message: MessageChain
    )