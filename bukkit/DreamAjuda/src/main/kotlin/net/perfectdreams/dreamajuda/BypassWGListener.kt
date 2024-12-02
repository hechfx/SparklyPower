package net.perfectdreams.dreamajuda

import net.perfectdreams.dreamcore.utils.SparklyNamespacedBooleanKey
import net.perfectdreams.dreamcore.utils.get
import net.perfectdreams.dreamcore.utils.remove
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent

class BypassWGListener : Listener {
    companion object {
        val BYPASS_WORLDGUARD_MOB_SPAWNING_DENY_KEY = SparklyNamespacedBooleanKey("bypass_worldguard_mob_spawning_deny")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun bypassWG(e: CreatureSpawnEvent) {
        if (e.isCancelled && e.entity.persistentDataContainer.get(BYPASS_WORLDGUARD_MOB_SPAWNING_DENY_KEY)) {
            e.isCancelled = false
            e.entity.persistentDataContainer.remove(BYPASS_WORLDGUARD_MOB_SPAWNING_DENY_KEY)
        }
    }
}