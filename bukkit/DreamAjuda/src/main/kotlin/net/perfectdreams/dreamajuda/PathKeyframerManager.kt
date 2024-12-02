package net.perfectdreams.dreamajuda

import kotlinx.coroutines.Job
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Husk
import org.bukkit.entity.Player

class PathKeyframerManager(val m: DreamAjuda) {
    val recordingAnimations = mutableMapOf<Player, SparklyAnimation>()
    val recordedAnimations = mutableMapOf<String, RecordedAnimation>()

    fun startRecording(player: Player) {
        recordingAnimations[player]?.finish()

        val animation = SparklyAnimation(
            this,
            Bukkit.getCurrentTick(),
            player.location,
            mutableMapOf()
        )

        recordingAnimations[player] = animation
        animation.job = m.launchMainThread {
            while (true) {
                animation.addAction(
                    Bukkit.getCurrentTick(),
                    AnimationAction.PlayerMovement(
                        RelativeLocation.toRelativeLocation(
                            animation.startLocation,
                            player.location
                        ),
                        player.bodyYaw,
                        player.isSneaking,
                        player.isSprinting,
                        player.pose,
                    )
                )
                delayTicks(1L)
            }
        }
    }

    fun finishRecording(player: Player): RecordedAnimation {
        val recording = recordingAnimations[player]!!
        recordingAnimations.remove(player, recording)
        return recording.finish()
    }

    fun playbackRecording(
        player: Player,
        npc: SparklyNPC,
        animation: RecordedAnimation,
        onKeyframeTick: (Int) -> (Unit)
    ): Job {
        val startLocation = animation.startLocation.toLocation(Bukkit.getWorld("RevampedTutorialIsland")!!)

        // val mkeyframes = animation.keyframes.entries.first()
        /* val npc = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
            m,
            (mkeyframes.value.filterIsInstance<AnimationAction.PlayerMovement>()).first().location.toLocation(startLocation),
            player.name,
            null
        ) */

        return m.launchMainThread {
            for (tick in 0 until animation.totalAnimationDuration) {
                Bukkit.broadcastMessage("Tick $tick of ${animation.totalAnimationDuration}")
                val actions = animation.keyframes[tick]

                if (actions != null) {
                    for (action in actions) {
                        when (action) {
                            AnimationAction.ArmSwing -> {
                                val packet = ClientboundAnimatePacket(
                                    (npc.getEntity()!! as CraftEntity).handle,
                                    ClientboundAnimatePacket.SWING_MAIN_HAND
                                )

                                (player as CraftPlayer).handle.connection.sendPacket(packet)
                            }

                            is AnimationAction.PlayerMovement -> {
                                npc.teleport(action.location.toLocation(startLocation))
                                (npc.getEntity() as Husk).bodyYaw = action.bodyYaw
                                npc.setSneaking(action.isSneaking)
                                npc.setPose(action.pose)

                                // npc.isSprinting = l.isSprinting
                            }

                            is AnimationAction.BlockBreak -> {
                                player.sendBlockChange(
                                    action.blockLocation.toLocation(startLocation),
                                    Bukkit.createBlockData(Material.AIR)
                                )
                                player.spawnParticle(
                                    Particle.BLOCK,
                                    action.blockLocation.toLocation(startLocation),
                                    10,
                                    Bukkit.createBlockData(Material.OAK_WOOD)
                                )
                            }

                            is AnimationAction.BlockBreakProgress -> {
                                player.sendBlockDamage(
                                    action.blockLocation.toLocation(startLocation),
                                    action.progress
                                )
                            }
                        }
                    }
                }

                onKeyframeTick.invoke(tick)

                delayTicks(1L)
            }
            return@launchMainThread
        }
    }
}