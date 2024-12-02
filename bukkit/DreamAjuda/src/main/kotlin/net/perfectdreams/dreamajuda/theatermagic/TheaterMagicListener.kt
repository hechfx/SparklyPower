package net.perfectdreams.dreamajuda.theatermagic

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent
import io.papermc.paper.event.player.PlayerArmSwingEvent
import net.perfectdreams.dreamcore.utils.extensions.isBetween
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntitySpawnEvent
import java.util.*

class TheaterMagicListener(val m: TheaterMagicManager) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onEntitySpawn(e: EntitySpawnEvent) {
        val entity = e.entity
        if (entity is Item) {
            m.recordingAnimations.forEach { t, u ->
                if (e.entity.location.isBetween(u.minBlockSnapshot, u.maxBlockSnapshot)) {
                    u.addAction(
                        Bukkit.getCurrentTick(),
                        AnimationAction.SpawnEntity(
                            e.entity.uniqueId.toString(),
                            AbsoluteLocation.toAbsoluteLocation(e.entity.location),
                            Base64.getEncoder().encodeToString(CraftMagicNumbers.INSTANCE.serializeEntity(e.entity))
                        )
                    )
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntitySpawn(e: EntityRemoveFromWorldEvent) {
        val entity = e.entity
        if (entity is Item) {
            m.recordingAnimations.forEach { t, u ->
                if (e.entity.location.isBetween(u.minBlockSnapshot, u.maxBlockSnapshot)) {
                    u.addAction(
                        Bukkit.getCurrentTick(),
                        AnimationAction.RemoveEntity(
                            e.entity.uniqueId.toString()
                        )
                    )
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(e: BlockBreakEvent) {
        m.getActiveRecording(e.player)?.addAction(
            Bukkit.getCurrentTick(),
            AnimationAction.BlockBreak(
                AbsoluteLocation.toAbsoluteLocation(e.block.location),
                e.block.blockData.asString
            )
        )
    }

    @EventHandler
    fun onArmSwing(e: PlayerArmSwingEvent) {
        m.getActiveRecording(e.player)?.addAction(
            Bukkit.getCurrentTick(),
            AnimationAction.ArmSwing(e.player.uniqueId.toString())
        )
    }

    @EventHandler
    fun onBlockProgress(e: BlockBreakProgressUpdateEvent) {
        val recordingAnimation = m.recordingAnimations[e.entity]

        recordingAnimation?.addAction(
            Bukkit.getCurrentTick(),
            AnimationAction.BlockBreakProgress(
                AbsoluteLocation.toAbsoluteLocation(e.block.location),
                e.progress
            )
        )
    }
}