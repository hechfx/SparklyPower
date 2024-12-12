package net.perfectdreams.dreamholograms.data

import kotlinx.serialization.Serializable
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.TextDisplay.TextAlignment
import org.joml.Matrix4f

@Serializable
sealed class HologramLine {
    @Serializable
    data class HologramText(
        var text: String,
        var billboard: Billboard,
        var backgroundColor: Int,
        var textShadow: Boolean,
        var lineWidth: Int,
        var seeThrough: Boolean,
        var textOpacity: Byte,
        var alignment: TextAlignment,
        @Serializable(with = Matrix4fSerializer::class)
        var transformation: Matrix4f,
        var brightness: Brightness?
    ) : HologramLine()

    @Serializable
    data class HologramItem(
        var serializedItem: String
    ) : HologramLine()
}