package net.perfectdreams.pantufa.listeners

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val bufferedImage = BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB)

    repeat(4) {
        val innerImage = BufferedImage(1, 4, BufferedImage.TYPE_INT_ARGB)

        val x = it % 4
        val y = it

        innerImage.setRGB(0, y, Color.WHITE.rgb)
        bufferedImage.createGraphics().drawImage(innerImage, x, 0, null)
    }

    ImageIO.write(bufferedImage, "png", File("C:\\Users\\leona\\AppData\\Roaming\\.minecraft\\resourcepacks\\SparklyPowerPlus\\assets\\sparklypower\\textures\\font\\chat_pixel_drawing.png"))
}