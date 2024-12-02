package net.perfectdreams.dreamajuda.theatermagic

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

@Serializable
data class AbsoluteLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float
) {
    companion object {
        fun toAbsoluteLocation(
            originalLocation: Location
        ): AbsoluteLocation {
            return AbsoluteLocation(
                originalLocation.x,
                originalLocation.y,
                originalLocation.z,
                originalLocation.yaw,
                originalLocation.pitch
            )
        }
    }

    fun toLocation(world: World) = Location(world, x, y, z, yaw, pitch)
}