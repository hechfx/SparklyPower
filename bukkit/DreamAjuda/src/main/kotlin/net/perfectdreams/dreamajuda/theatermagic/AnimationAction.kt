package net.perfectdreams.dreamajuda.theatermagic

import kotlinx.serialization.Serializable
import net.perfectdreams.dreamajuda.AbsoluteLocation
import org.bukkit.entity.Entity
import org.bukkit.entity.Pose
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

@Serializable
sealed class AnimationAction {
    @Serializable
    data class ArmSwing(val uniqueId: String) : AnimationAction()

    @Serializable
    class PlayerMovement(
        val uniqueId: String,
        val location: AbsoluteLocation,
        val bodyYaw: Float,
        val isSneaking: Boolean,
        val isSprinting: Boolean,
        val pose: Pose
    ) : AnimationAction()

    @Serializable
    data class BlockBreakProgress(
        val blockLocation: AbsoluteLocation,
        val progress: Float
    ) : AnimationAction()

    @Serializable
    class UpdatedBlock(
        val location: AbsoluteLocation,
        val blockData: String
    ) : AnimationAction()

    @Serializable
    class SpawnEntity(
        val uniqueId: String,
        val location: AbsoluteLocation,
        val serializedEntity: String
    ) : AnimationAction()

    @Serializable
    class RemoveEntity(val uniqueId: String) : AnimationAction()

    @Serializable
    class BlockBreak(
        val blockLocation: AbsoluteLocation,
        val blockData: String
    ) : AnimationAction()

    @Serializable
    class ActiveEquipment(
        val uniqueId: String,
        val slot: EquipmentSlot,
        val itemStack: String?
    ) : AnimationAction()
}