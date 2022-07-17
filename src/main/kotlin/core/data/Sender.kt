package com.github.core.data

import java.awt.image.BufferedImage


data class Sender(
    val name:String,
    val avatarProvider:(() -> BufferedImage),
    val id:Long,
    val title:String,
    val titleColor:String,
)