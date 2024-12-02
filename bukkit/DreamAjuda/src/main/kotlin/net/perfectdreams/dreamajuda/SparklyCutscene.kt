package net.perfectdreams.dreamajuda

import kotlinx.coroutines.Job
import net.minecraft.server.level.ServerEntity
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftArmorStand
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.entity.CraftTextDisplay
import org.bukkit.entity.*

class SparklyCutscene(val player: Player) {
    var cameraEntity: TextDisplay? = null
    val cameraLocation
        get() = cameraEntity!!.location

    fun start() {
        player.gameMode = GameMode.SPECTATOR
    }

    /**
     * Sets a new camera location
     */
    fun setCameraLocation(location: Location) {
        // We need to teleport the player because maybe the entity is outside the currently loaded chunks
        player.teleport(location)

        val world = location.world

        val camera = world.spawnEntity(
            location,
            EntityType.TEXT_DISPLAY
        ) as TextDisplay

        player.spectatorTarget = camera

        // Remove the old camera entity
        this.cameraEntity?.remove()
        this.cameraEntity = camera
    }

    /**
     * Moves the camera
     */
    fun moveCamera(location: Location) {
        cameraEntity?.teleport(location)
    }

    fun easeCamera(
        m: DreamAjuda,
        points: List<InterpolationKeyframe>
    ): Job {
        return m.launchMainThread {
            var idx = 0
            while (true) {
                if (points.size - 1 == idx)
                    return@launchMainThread

                val point0 = points[idx]
                val point1 = points[idx + 1]

                easeCamera(m, point0.location, point1.location, point1.duration!!).join()
                idx++
            }
        }
    }

    /**
     * Eases the camera
     */
    fun easeCamera(
        m: DreamAjuda,
        originalLocation: Location,
        targetLocation: Location,
        targetTicks: Int
    ): Job {
        val targetTicksDouble = targetTicks.toDouble()
        return m.launchMainThread {
            var remainingTicks = targetTicks
            while (remainingTicks > 0) {
                val delayTicks = remainingTicks.coerceAtMost(59)
                cameraEntity?.teleportDuration = delayTicks
                val craft = (cameraEntity as CraftTextDisplay).handle

                // We need to force the entity data to be refreshed before the entity is teleported, to avoid an unnecessary delayTicks(1L)
                craft.refreshEntityData((player as CraftPlayer).handle)

                // Yes we need to delay it after updating the teleportDuration smh
                // delayTicks(1L)

                remainingTicks = (remainingTicks - 59).coerceAtLeast(0)

                val elapsedTicks = targetTicks - remainingTicks
                val newX = easeLinear(originalLocation.x, targetLocation.x, elapsedTicks / targetTicksDouble)
                val newY = easeLinear(originalLocation.y, targetLocation.y, elapsedTicks / targetTicksDouble)
                val newZ = easeLinear(originalLocation.z, targetLocation.z, elapsedTicks / targetTicksDouble)
                val newYaw = easeLinear(originalLocation.yaw.toDouble(), targetLocation.yaw.toDouble(), elapsedTicks / targetTicksDouble).toFloat()
                val newPitch = easeLinear(originalLocation.pitch.toDouble(), targetLocation.pitch.toDouble(), elapsedTicks / targetTicksDouble).toFloat()

                Bukkit.broadcastMessage("Elapsed Ticks: $elapsedTicks - Remaining Ticks: $remainingTicks")
                cameraEntity?.teleport(
                    Location(
                        targetLocation.world,
                        newX,
                        newY,
                        newZ,
                        newYaw,
                        newPitch
                    )
                )

                delayTicks(delayTicks.toLong())
            }

            cameraEntity?.teleport(targetLocation)
        }
    }

    /**
     * Finished the cutscene
     */
    fun end() {
        cameraEntity?.remove()
    }

    private fun easeLinear(start: Double, end: Double, percent: Double): Double {
        return start+(end-start)*percent
    }
}