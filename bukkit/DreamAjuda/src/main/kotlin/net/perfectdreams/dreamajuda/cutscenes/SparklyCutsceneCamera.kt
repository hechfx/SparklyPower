package net.perfectdreams.dreamajuda.cutscenes

import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.Job
import net.minecraft.network.protocol.BundlePacket
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.phys.Vec3
import net.perfectdreams.dreamajuda.CameraPathPoint
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.extensions.sendPacket
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

// WE USE PACKETS BECAUSE WE ARE BUILT LIKE THAT FR FR
// (the real reason is due to the server's pos/rot periodic sync fucking up the interpolation)
class SparklyCutsceneCamera(val m: DreamAjuda, val player: Player) {
    val allocatedEntityId = Entity.nextEntityId()
    private var alreadySpawnedAndSpectating = false

    /**
     * Teleports the camera to a new location, easing by [easingTicks]
     *
     * If the [easingTicks] is zero, the camera is recreated from scratch to avoid any previous set easings affecting the entity
     *
     * The caller has the responsability of waiting the easing to end
     */
    fun setCameraLocation(location: Location, easingTicks: Int) {
        if (!alreadySpawnedAndSpectating || easingTicks == 0) {
            // We ALWAYS recreate the camera on 0 easing ticks, because there is an issue where, even if you send the packets in the right order (metadata -> teleport), the client may process it out of order and cause issues
            Bukkit.broadcastMessage("Creating new camera, setting $easingTicks ticks for easing (bundle)")

            val add = ClientboundAddEntityPacket(
                allocatedEntityId,
                UUID.randomUUID(),
                location.x,
                location.y,
                location.z,
                location.pitch,
                location.yaw,
                EntityType.TEXT_DISPLAY,
                0,
                Vec3.ZERO,
                0.0
            )

            val metadata = ClientboundSetEntityDataPacket(
                allocatedEntityId,
                listOf(
                    SynchedEntityData.DataValue(Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID.id, EntityDataSerializers.INT, easingTicks),
                    SynchedEntityData.DataValue(
                        23,
                        EntityDataSerializers.COMPONENT,
                        PaperAdventure.asVanilla(
                            textComponent("test packet") {}
                        )
                    )
                )
            )

            val gameModeChange = ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, 3f)

            val spectate = ClientboundSetCameraPacket((player as CraftPlayer).handle)
            // TODO: Add this to the helpful NMS packet changes
            val f = ClientboundSetCameraPacket::class.java.getDeclaredField("cameraId")
            f.isAccessible = true
            f.set(spectate, allocatedEntityId)

            // We use bundles because they look cool ok
            val bundle = ClientboundBundlePacket(listOf(add, metadata, gameModeChange, spectate))
            player.sendPacket(bundle)

            this.alreadySpawnedAndSpectating = true
        } else {
            Bukkit.broadcastMessage("Updating camera location, using $easingTicks ticks for easing (bundle)")
            val metadata = ClientboundSetEntityDataPacket(
                allocatedEntityId,
                listOf(
                    SynchedEntityData.DataValue(
                        Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID.id,
                        EntityDataSerializers.INT,
                        easingTicks
                    ),
                )
            )

            val teleport = ClientboundTeleportEntityPacket(
                allocatedEntityId,
                PositionMoveRotation(
                    Vec3(
                        location.x,
                        location.y,
                        location.z
                    ),
                    Vec3.ZERO,
                    location.yaw,
                    location.pitch
                ),
                setOf(),
                false
            )

            // We use bundles because they look cool ok
            val bundle = ClientboundBundlePacket(listOf(metadata, teleport))
            player.sendPacket(bundle)
        }
    }

    /**
     * Eases a camera over points
     */
    fun easeCamera(
        m: DreamAjuda,
        startingLocation: Location,
        paths: List<CameraPathPoint>
    ): Job {
        return m.launchMainThread {
            setCameraLocation(startingLocation, 0)

            for (path in paths) {
                setCameraLocation(path.point, path.duration)
                delayTicks(path.duration.toLong())
            }
        }
    }

    /**
     * Removes the currently active camera and resets the player
     */
    fun remove() {
        // We need to reset the camera to the player when we need to remove the original
        player.sendPacket(ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, player.gameMode.value.toFloat()))
        player.sendPacket(ClientboundSetCameraPacket((player as CraftPlayer).handle))
        player.sendPacket(ClientboundRemoveEntitiesPacket(allocatedEntityId))
        this.alreadySpawnedAndSpectating = false
        // TODO: Reset the player?
        // camera?.cameraEntity?.remove()
    }
}