package net.perfectdreams.dreamholograms.data

import kotlinx.serialization.Serializable
import net.perfectdreams.dreamholograms.DreamHologram
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.TextDisplay.TextAlignment
import org.joml.Matrix4f

@Serializable
sealed class HologramLine {
    @Serializable
    data class HologramText(
        var text: String,
        var billboard: Billboard = Billboard.CENTER,
        var backgroundColor: Int = StoredHologram.DEFAULT_BACKGROUND_COLOR,
        var textShadow: Boolean = false,
        var lineWidth: Int = Int.MAX_VALUE,
        var seeThrough: Boolean = false,
        var textOpacity: Byte = -1,
        var alignment: TextAlignment = TextAlignment.CENTER,
        @Serializable(with = Matrix4fSerializer::class)
        var transformation: Matrix4f = Matrix4f(),
        var brightness: Brightness? = null
    ) : HologramLine()

    @Serializable
    data class HologramItem(
        var serializedItem: String
    ) : HologramLine()
}