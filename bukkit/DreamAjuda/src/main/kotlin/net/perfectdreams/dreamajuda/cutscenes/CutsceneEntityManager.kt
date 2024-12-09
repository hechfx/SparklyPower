package net.perfectdreams.dreamajuda.cutscenes

import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Husk
import org.bukkit.entity.Player

class CutsceneEntityManager(val m: DreamAjuda, val player: Player) {
    /**
     * Spawns an entity at the specified location, the entity will be visible only for the [player], and it won't be persistent
     */
    fun <T : Entity> spawnEntity(location: Location, clazz: Class<T>, function: (T) -> (Unit) = {}): T {
        return location.world.spawn(location, clazz) {
            it.isVisibleByDefault = false
            it.isPersistent = false
            player.showEntity(m, it)

            function.invoke(it)
        }
    }

    /**
     * Spawns an entity at the specified location, the entity will be visible only for the [player]
     */
    fun spawnFakePlayer(
        location: Location,
        name: String,
        skinTextures: SkinTexture? = null,
        preSpawnSettings: (entity: Husk, npc: SparklyNPC) -> (Unit) = { _, _ -> }
    ): SparklyNPC {
        return DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
            m,
            location,
            name,
            skinTextures
        ) { entity, npc ->
            npc.setVisibleByDefault(false)
            player.showEntity(m, entity)

            preSpawnSettings.invoke(entity, npc)
        }
    }
}