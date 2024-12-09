package net.perfectdreams.dreamcore.serializable

import io.papermc.paper.math.Position
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

@Serializable
data class SerializedWorldBlockPosition(
    val worldName: String,
    val x: Int,
    val y: Int,
    val z: Int
) {
    fun toLocation() = Location(null, x.toDouble(), y.toDouble(), z.toDouble())

    fun toLocation(world: World) = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

    fun toPosition() = Position.block(x, y, z)
}