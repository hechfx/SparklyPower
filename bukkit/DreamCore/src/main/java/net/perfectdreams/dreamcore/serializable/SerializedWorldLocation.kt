package net.perfectdreams.dreamcore.serializable

import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

@Serializable
data class SerializedWorldLocation(
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float
) {
    fun toLocation() = Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch)

    fun toLocation(world: World) = Location(world, x, y, z, yaw, pitch)
}