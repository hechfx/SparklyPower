package net.perfectdreams.dreamajuda.cutscenes

import io.papermc.paper.entity.TeleportFlag
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.perfectdreams.dreamajuda.CameraPathPoint
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.entity.CraftTextDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * A cutscene
 */
abstract class SparklyCutsceneFinalCut(
    val m: DreamAjuda,
    val player: Player,
    val cutsceneCamera: SparklyCutsceneCamera
) {
    /**
     * Starts the current cutscene
     */
    abstract suspend fun start()

    /**
     * Cleans up the current cutscene
     */
    open fun cleanUp() {}

    /**
     * Finishes the current cutscene
     */
    fun end(removeActiveCamera: Boolean) {
        if (removeActiveCamera) {
            player.gameMode = GameMode.SURVIVAL
            cutsceneCamera.remove()
        }
    }

    /**
     * Teleports the camera to a new location
     */
    /* suspend fun setCameraLocation(location: Location) {
        // We need to teleport the player because maybe the entity is outside the currently loaded chunks
        // player.teleport(location)

        // this.cameraEntity.teleportDuration = 0
        // We need to reset the current interpolation to avoid any easing...

        craftCameraEntity.entityData.set(net.minecraft.world.entity.Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID, 0)

        // Force refresh
        craftCameraEntity.refreshEntityData((player as CraftPlayer).handle)

        this.cameraEntity.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS)
        // val craft = (cameraEntity as CraftTextDisplay).handle
        // We need to force the entity data to be refreshed before the entity is teleported, to avoid an unnecessary delayTicks(1L)
        // craft.refreshEntityData((player as CraftPlayer).handle)
    } */

    /**
     * Runs a specific cutscene
     */
    suspend fun runCutscene(cutscene: SparklyCutsceneFinalCut, removeActiveCamera: Boolean) {
        cutscene.start()
        cutscene.end(removeActiveCamera)
    }

    /* fun easeCamera(
        m: DreamAjuda,
        paths: List<CameraPathPoint>
    ): Job {
        return m.launchMainThread {
            for (path in paths) {
                if (path.duration == null) {
                    craftCameraEntity.entityData.set(net.minecraft.world.entity.Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID, 0)

                    delay(1L)

                    cameraEntity.teleport(path.point)

                    continue
                }


                // REQUIRES SPARKLYPAPER!!! SparklyPaper changes the pos/rot sync to not trigger for display entities when the pos/rot has not changed, this allows
                // interpolating for longer ticks
                craftCameraEntity.entityData.set(net.minecraft.world.entity.Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID, path.duration)

                // Send the new location after one tick
                delayTicks(1L)

                cameraEntity.teleport(path.point)

                delayTicks(path.duration - 1L)
            }
        }
    } */

    /* fun easeCamera(
        m: DreamAjuda,
        paths: List<CameraInterpolationKeyframe>
    ): Job {
        val TARGET_TELEPORT_DURATION_TICKS = 59

        return m.launchMainThread {
            for (path in paths) {
                cameraEntity.teleport(path.startLocation)

                var remainingTicks = path.duration
                val targetDurationDouble = remainingTicks.toDouble()

                while (true) {
                    var currentLoopTicks: Int

                    Bukkit.broadcastMessage("Remaining Ticks: $remainingTicks")

                    if (remainingTicks == 0) {
                        Bukkit.broadcastMessage("No remaining ticks, finished path!")
                        break
                    }

                    if (remainingTicks >= TARGET_TELEPORT_DURATION_TICKS) {
                        remainingTicks -= TARGET_TELEPORT_DURATION_TICKS
                        currentLoopTicks = TARGET_TELEPORT_DURATION_TICKS
                    } else {
                        currentLoopTicks = remainingTicks
                        remainingTicks = 0
                    }

                    val elapsedTicks = path.duration - remainingTicks

                    Bukkit.broadcastMessage("Current Loop Ticks: $currentLoopTicks - Remaining Ticks: $remainingTicks")

                    cameraEntity.teleportDuration = currentLoopTicks
                    delayTicks(1L)

                    val newX = easeLinear(path.startLocation.x, path.finishLocation.x, elapsedTicks / targetDurationDouble)
                    val newY = easeLinear(path.startLocation.y, path.finishLocation.y, elapsedTicks / targetDurationDouble)
                    val newZ = easeLinear(path.startLocation.z, path.finishLocation.z, elapsedTicks / targetDurationDouble)
                    val newYaw = easeLinear(path.startLocation.yaw.toDouble(), path.finishLocation.yaw.toDouble(), elapsedTicks / targetDurationDouble).toFloat()
                    val newPitch = easeLinear(path.startLocation.pitch.toDouble(), path.finishLocation.pitch.toDouble(), elapsedTicks / targetDurationDouble).toFloat()

                    cameraEntity.teleport(
                        Location(
                            path.finishLocation.world,
                            newX,
                            newY,
                            newZ,
                            newYaw,
                            newPitch
                        )
                    )

                    delayTicks(currentLoopTicks.toLong() - 8L)
                }
            }
        }
    } */

    /* fun easeCamera(
        m: DreamAjuda,
        points: List<InterpolationKeyframe>
    ): Job {
        val hasAnyInvalidDurations = points.drop(1).any { it.duration == null }
        if (hasAnyInvalidDurations)
            error("There is a null interpolation duration on one of the keyframes! Only the first keyframe can have a null duration!")

        return m.launchMainThread {
            cameraEntity.teleportDuration = 0
            cameraEntity.interpolationDelay = 20
            val craft = (cameraEntity as CraftTextDisplay).handle
            craft.refreshEntityData((player as CraftPlayer).handle)
            delayTicks(10L)
            cameraEntity.teleport(points.first().location)
            delayTicks(10L)

            for (i in 0 until points.size - 1) {
                val point0 = points[i]

                // Bukkit.broadcastMessage("Processing keyframe index $i")

                val point1 = points[i + 1]

                // Bukkit.broadcastMessage("Point to Point (point0: $point0) to (point1: $point1)")

                var remainingTicks = point1.duration!!
                val targetDurationDouble = point1.duration.toDouble()
                val TARGET_TELEPORT_DURATION_TICKS = 59

                while (remainingTicks > 0) {
                    // We need to calculate the NEXT point in time here, not the current
                    val currentLoopTeleportDuration = remainingTicks.coerceAtMost(TARGET_TELEPORT_DURATION_TICKS)
                    remainingTicks -= currentLoopTeleportDuration
                    val elapsedTicks = point1.duration - remainingTicks

                    Bukkit.broadcastMessage("Current Loop Teleport Duration: $currentLoopTeleportDuration - Remaining Ticks: $remainingTicks - Progress: ${elapsedTicks / targetDurationDouble}")

                    // The teleport is triggered 1 tick AFTER the update (I mean... the teleport itself)
                    // However, because display entities are WONKY AS HELL, if we take more than 1 tick to send the teleport, it WILL be slow at the end of the teleport at random keyframes (go figure why)
                    // That's why we don't delay by one tick
                    cameraEntity.teleportDuration = currentLoopTeleportDuration
                    val craft = (cameraEntity as CraftTextDisplay).handle
                    craft.refreshEntityData((player as CraftPlayer).handle)

                    // delayTicks(1L)

                    // We need to remove it NOW because we need to calculate how much it would ease
                    // remainingTicks -= currentLoopTeleportDuration
                    // val elapsedTicks = point1.duration - remainingTicks

                    val newX = easeLinear(point0.location.x, point1.location.x, elapsedTicks / targetDurationDouble)
                    val newY = easeLinear(point0.location.y, point1.location.y, elapsedTicks / targetDurationDouble)
                    val newZ = easeLinear(point0.location.z, point1.location.z, elapsedTicks / targetDurationDouble)
                    val newYaw = easeLinear(point0.location.yaw.toDouble(), point1.location.yaw.toDouble(), elapsedTicks / targetDurationDouble).toFloat()
                    val newPitch = easeLinear(point0.location.pitch.toDouble(), point1.location.pitch.toDouble(), elapsedTicks / targetDurationDouble).toFloat()

                    cameraEntity.teleport(
                        Location(
                            point1.location.world,
                            newX,
                            newY,
                            newZ,
                            newYaw,
                            newPitch
                        )
                    )

                    // Because it is delayed by one tick, we need to update it 1 tick BEFORE the end
                    delayTicks(currentLoopTeleportDuration - 1L)
                }
            }
        }
    } */

    /**
     * Eases the camera
     */
    /* fun easeCamera(
        m: DreamAjuda,
        originalLocation: Location,
        targetLocation: Location,
        targetTicks: Int
    ): Job {
        val targetTicksDouble = targetTicks.toDouble()

        return m.launchMainThread {
            var remainingTicks = targetTicks
            while (remainingTicks > 0) {
                val delayTicks = remainingTicks.coerceAtMost(30)
                cameraEntity.teleportDuration = delayTicks
                delayTicks(1L)
                Bukkit.broadcastMessage("Delaying ticks again")

                val craft = (cameraEntity as CraftTextDisplay).handle

                // We need to force the entity data to be refreshed before the entity is teleported, to avoid an unnecessary delayTicks(1L)
                // craft.refreshEntityData((player as CraftPlayer).handle)

                // Yes we need to delay it after updating the teleportDuration smh
                // delayTicks(1L)

                remainingTicks = (remainingTicks - 30).coerceAtLeast(0)

                val elapsedTicks = targetTicks - remainingTicks
                val newX = easeLinear(originalLocation.x, targetLocation.x, elapsedTicks / targetTicksDouble)
                val newY = easeLinear(originalLocation.y, targetLocation.y, elapsedTicks / targetTicksDouble)
                val newZ = easeLinear(originalLocation.z, targetLocation.z, elapsedTicks / targetTicksDouble)
                val newYaw = easeLinear(originalLocation.yaw.toDouble(), targetLocation.yaw.toDouble(), elapsedTicks / targetTicksDouble).toFloat()
                val newPitch = easeLinear(originalLocation.pitch.toDouble(), targetLocation.pitch.toDouble(), elapsedTicks / targetTicksDouble).toFloat()

                Bukkit.broadcastMessage("Elapsed Ticks: $elapsedTicks - Remaining Ticks: $remainingTicks - Delaying by $delayTicks ticks")
                cameraEntity.teleport(
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

            Bukkit.broadcastMessage("Finished camera ease!")
            cameraEntity.teleport(targetLocation)
        }
    } */

    private fun easeLinear(start: Double, end: Double, percent: Double): Double {
        return start+(end-start)*percent
    }
}