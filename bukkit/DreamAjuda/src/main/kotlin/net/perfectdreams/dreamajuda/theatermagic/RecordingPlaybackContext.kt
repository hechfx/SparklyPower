package net.perfectdreams.dreamajuda.theatermagic

import kotlinx.coroutines.Job
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.commands.TutorialExecutor
import net.perfectdreams.dreamcore.utils.ItemUtils
import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.Entity
import org.bukkit.entity.Husk
import org.bukkit.entity.Item
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

    fun startPlayback(
        ticksRange: IntRange? = null,
        onKeyframeTick: suspend (Int) -> (Unit)
    ): Job {
        return m.launchMainThread {
            startPlaybackOnCurrentCoroutine(ticksRange, onKeyframeTick)
        }
    }

    suspend fun startPlaybackOnCurrentCoroutine(
        ticksRange: IntRange? = null,
        onKeyframeTick: suspend (Int) -> (Unit)
    ) {
        val totalAnimationDuration = storedRecording.keyframes.keys.max()

        /* for (b in storedRecording.blocksToBeRestored) {
            world.setBlockData(
                b.x,
                b.y,
                b.z,
                Bukkit.createBlockData(b.blockData)
            )
        } */

        val minTick = ticksRange?.start ?: 0
        val maxTick = ticksRange?.endInclusive ?: totalAnimationDuration

        for (tick in minTick..maxTick) {
            if (player.name == "MrPowerGamerBR")
                player.sendActionBar("Tick $tick of $maxTick (total: $totalAnimationDuration)")
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
                            if (TutorialExecutor.COPY_WORLD_FROM_TEMPLATE_WORLD) {
                                world.getBlockAt(
                                    action.location.x.toInt(),
                                    action.location.y.toInt(),
                                    action.location.z.toInt(),
                                ).setBlockData(
                                    Bukkit.createBlockData(action.blockData),
                                    false
                                )
                            } else {
                                player.sendBlockChange(action.location.toLocation(world), Bukkit.createBlockData(action.blockData))
                            }
                        }

                        is AnimationAction.BlockBreakProgress -> {
                            player.sendBlockDamage(
                                action.blockLocation.toLocation(world),
                                action.progress
                            )
                        }

                        is AnimationAction.SpawnEntity -> {
                            if (TutorialExecutor.COPY_WORLD_FROM_TEMPLATE_WORLD) {
                                val entity = CraftMagicNumbers.INSTANCE.deserializeEntity(
                                    Base64.getDecoder().decode(action.serializedEntity), world
                                )
                                // Bukkit.broadcastMessage("Spawning entity ${entity.type} at... ${action.location.toLocation(world)}")
                                entity.spawnAt(action.location.toLocation(world))
                                recordingIdToRealEntity[action.uniqueId] = entity
                            } else {
                                val entity = CraftMagicNumbers.INSTANCE.deserializeEntity(
                                    Base64.getDecoder().decode(action.serializedEntity), world
                                )

                                if (entity is Item) {
                                    entity.setCanMobPickup(false)
                                    entity.setCanPlayerPickup(false)
                                }

                                entity.isVisibleByDefault = false
                                player.showEntity(m, entity)
                                // Bukkit.broadcastMessage("Spawning entity ${entity.type} at... ${action.location.toLocation(world)}")
                                entity.spawnAt(action.location.toLocation(world))
                                recordingIdToRealEntity[action.uniqueId] = entity
                            }
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
                            val npc = playerProvider.invoke(action.uniqueId)

                            npc.equipment.setItem(
                                action.slot,
                                action.itemStack?.let { ItemUtils.deserializeItemFromBase64(it) }
                            )
                        }
                    }
                }
            }

            onKeyframeTick.invoke(tick)

            delayTicks(1L)
        }

        // Bukkit.broadcastMessage("Finished animation")
    }
}