package net.perfectdreams.dreamajuda

import kotlinx.serialization.Serializable
import net.minecraft.world.level.levelgen.VerticalAnchor.Absolute
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Pose

@Serializable
sealed class AnimationAction {
    @Serializable
    data object ArmSwing : AnimationAction()

    @Serializable
    data class BlockBreakProgress(
        val blockLocation: RelativeLocation,
        val progress: Float
    ) : AnimationAction()

    @Serializable
    data class BlockBreak(
        val blockLocation: RelativeLocation,
    ) : AnimationAction()

    @Serializable
    class PlayerMovement(
        val location: RelativeLocation,
        val bodyYaw: Float,
        val isSneaking: Boolean,
        val isSprinting: Boolean,
        val pose: Pose
    ) : AnimationAction()
}

@Serializable
data class RelativeLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float
) {
    companion object {
        fun toRelativeLocation(
            originalLocation: Location,
            location: Location
        ): RelativeLocation {
            return RelativeLocation(
                location.x - originalLocation.x,
                location.y - originalLocation.y,
                location.z - originalLocation.z,
                location.yaw - originalLocation.yaw,
                location.pitch - originalLocation.pitch
            )
        }
    }

    fun toLocation(originalLocation: Location) = Location(originalLocation.world, originalLocation.x + x, originalLocation.y + y, originalLocation.z + z, originalLocation.yaw + yaw, originalLocation.pitch + pitch)
}

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