package com.github

import com.github.core.Paints
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val image = Paints.paintTextMessage(ImageIO.read(File("I@X]V_B_EI)W4PGAW_J(3H1.bmp")),"cxxsh","草死你麻辣麻辣麻辣麻辣啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊")
    ImageIO.write(image,"png",File("test.png"))
}