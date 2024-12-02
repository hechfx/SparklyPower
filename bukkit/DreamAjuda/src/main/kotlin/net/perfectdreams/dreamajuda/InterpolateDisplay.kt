package net.perfectdreams.dreamajuda

import kotlinx.coroutines.Job
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.entity.CraftTextDisplay
import org.bukkit.entity.Display

private fun easeLinear(start: Double, end: Double, percent: Double): Double {
    return start+(end-start)*percent
}

data class InterpolationKeyframe(
    val location: Location,
    val duration: Int?
)

data class CameraPathPoint(
    val point: Location,
    val duration: Int
)

fun interpolateTeleport(
    m: DreamAjuda,
    display: Display,
    targetLocation: Location,
    targetTicks: Int
): Job {
    val originalLocation = display.location
    val targetTicksDouble = targetTicks.toDouble()

    return m.launchMainThread {
        var remainingTicks = targetTicks
        while (remainingTicks > 0) {
            val delayTicks = remainingTicks.coerceAtMost(59)
            display.teleportDuration = delayTicks
            val craft = (display as CraftTextDisplay).handle

            // We need to force the entity data to be refreshed before the entity is teleported, to avoid an unnecessary delayTicks(1L)
            originalLocation.world.players.forEach {
                craft.refreshEntityData((it as CraftPlayer).handle)
            }

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
            display.teleport(
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

        display.teleport(targetLocation)
    }
}