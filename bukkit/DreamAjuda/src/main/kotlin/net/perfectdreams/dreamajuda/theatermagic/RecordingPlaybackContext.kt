package net.perfectdreams.dreamajuda.theatermagic

import kotlinx.coroutines.Job
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamcore.utils.ItemUtils
import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.Entity
import org.bukkit.entity.Husk
import org.bukkit.entity.Player
import java.util.*

class RecordingPlaybackContext(
    val m: DreamAjuda,
    val player: Player,
    val storedRecording: TheaterMagicStoredRecordingAnimation,
    val world: World,
    val playerProvider: (String) -> (SparklyNPC)
) {
    val recordingIdToRealEntity = mutableMapOf<String, Entity>()

    fun startPlayback(onKeyframeTick: (Int) -> (Unit)): Job {
        val totalAnimationDuration = storedRecording.keyframes.keys.max()

        /* for (b in storedRecording.blocksToBeRestored) {
            world.setBlockData(
                b.x,
                b.y,
                b.z,
                Bukkit.createBlockData(b.blockData)
            )
        } */

        return m.launchMainThread {
            val accumulatedEquipmentActions = mutableListOf<AnimationAction.ActiveEquipment>()

            for (tick in 0 until totalAnimationDuration) {
                Bukkit.broadcastMessage("Tick $tick of $totalAnimationDuration")
                val keyframe = storedRecording.keyframes[tick]

                if (keyframe != null) {
                    for (action in keyframe.actions) {
                        when (action) {
                            is AnimationAction.ArmSwing -> {
                                val npc = playerProvider.invoke(action.uniqueId)

                                npc.swingMainHand()
                            }

                            is AnimationAction.PlayerMovement -> {
                                val npc = playerProvider.invoke(action.uniqueId)

                                npc.teleport(action.location.toLocation(world))
                                (npc.getEntity() as Husk).bodyYaw = action.bodyYaw
                                npc.setSneaking(action.isSneaking)
                                npc.setPose(action.pose)

                                // npc.isSprinting = l.isSprinting
                            }

                            is AnimationAction.UpdatedBlock -> {
                                world.setBlockData(
                                    action.location.x.toInt(),
                                    action.location.y.toInt(),
                                    action.location.z.toInt(),
                                    Bukkit.createBlockData(action.blockData)
                                )
                            }

                            is AnimationAction.BlockBreakProgress -> {
                                player.sendBlockDamage(
                                    action.blockLocation.toLocation(world),
                                    action.progress
                                )
                            }

                            is AnimationAction.SpawnEntity -> {
                                val entity = CraftMagicNumbers.INSTANCE.deserializeEntity(Base64.getDecoder().decode(action.serializedEntity), world)
                                Bukkit.broadcastMessage("Spawning entity ${entity.type} at... ${action.location.toLocation(world)}")
                                entity.spawnAt(action.location.toLocation(world))
                                recordingIdToRealEntity[action.uniqueId] = entity
                            }

                            is AnimationAction.RemoveEntity -> {
                                recordingIdToRealEntity[action.uniqueId]?.remove()
                                recordingIdToRealEntity.remove(action.uniqueId)
                            }

                            is AnimationAction.BlockBreak -> {
                                player.spawnParticle(
                                    Particle.BLOCK,
                                    action.blockLocation.toLocation(world).add(0.5, 0.5, 0.5),
                                    10,
                                    Bukkit.createBlockData(action.blockData)
                                )
                            }

                            is AnimationAction.ActiveEquipment -> {
                                // A hacky workaround because for some reason the equipment is not sent to the user on the first ticks...
                                // (Maybe it is due to the bundle packet?)
                                if (5 > tick) {
                                    accumulatedEquipmentActions.add(action)
                                } else {
                                    val npc = playerProvider.invoke(action.uniqueId)

                                    npc.equipment.setItem(
                                        action.slot,
                                        action.itemStack?.let { ItemUtils.deserializeItemFromBase64(it) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (tick == 5) {
                    for (action in accumulatedEquipmentActions) {
                        val npc = playerProvider.invoke(action.uniqueId)

                        npc.equipment.setItem(
                            action.slot,
                            action.itemStack?.let { ItemUtils.deserializeItemFromBase64(it) }
                        )
                    }
                    accumulatedEquipmentActions.clear()
                }

                onKeyframeTick.invoke(tick)

                delayTicks(1L)
            }
        }
    }
}