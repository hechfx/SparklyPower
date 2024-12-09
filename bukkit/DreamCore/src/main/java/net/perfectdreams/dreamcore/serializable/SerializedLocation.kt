package net.perfectdreams.dreamcore.serializable

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

@Serializable
data class SerializedLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float
) {
    fun toLocation() = Location(null, x, y, z, yaw, pitch)

    fun toLocation(world: World) = Location(world, x, y, z, yaw, pitch)
}