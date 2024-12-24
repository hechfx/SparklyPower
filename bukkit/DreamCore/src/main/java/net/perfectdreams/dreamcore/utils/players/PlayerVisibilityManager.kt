package net.perfectdreams.dreamcore.utils.players

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.perfectdreams.dreamcore.utils.extensions.sendPacket
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.lang.ref.WeakReference
import java.util.*

/**
 * Hides a player from the world, but does not hide them from the player list
 */
class PlayerVisibilityManager(val selfPlayer: Player) {
    companion object {
        private val pluginWeakReferences: WeakHashMap<Plugin, WeakReference<Plugin>> = WeakHashMap()

        private fun getPluginWeakReference(plugin: Plugin): WeakReference<Plugin> {
            return pluginWeakReferences.computeIfAbsent(plugin) { referent: Plugin -> WeakReference(referent) }
        }
    }

    private val invertedVisibilityEntities: MutableMap<UUID, MutableSet<WeakReference<Plugin>>> = Object2ObjectOpenHashMap()

    fun isHidden(player: Player): Boolean {
        return invertedVisibilityEntities.contains(player.uniqueId)
    }

    fun isHiddenByPlugin(plugin: Plugin, player: Player): Boolean {
        return invertedVisibilityEntities[player.uniqueId]?.contains(PlayerVisibilityManager.getPluginWeakReference(plugin)) == true
    }

    fun hidePlayer(plugin: Plugin, player: Player) {
        // Don't attempt to hide yourself silly
        if (player == this.selfPlayer)
            return

        val shouldHide = addInvertedVisibility(plugin, player)

        if (shouldHide) {
            // Remove the player from our perspective
            selfPlayer.sendPacket(ClientboundRemoveEntitiesPacket(player.entityId))
        }
    }

    fun showPlayer(plugin: Plugin, player: Player) {
        // Don't attempt to show yourself silly
        if (player == this.selfPlayer)
            return

        val shouldShow = removeInvertedVisibility(plugin, player)
        if (shouldShow) {
            // TODO: How to unhide the player?
            // TODO: Maybe do this in a better way...
            player.playerProfile = player.playerProfile
        }
    }

    private fun addInvertedVisibility(plugin: Plugin, entity: Entity): Boolean {
        var invertedPlugins: MutableSet<WeakReference<Plugin>>? = invertedVisibilityEntities[entity.uniqueId]
        if (invertedPlugins != null) {
            // Some plugins are already inverting the entity. Just mark that this
            // plugin wants the entity inverted too and end.
            invertedPlugins.add(PlayerVisibilityManager.getPluginWeakReference(plugin))
            return false
        }
        invertedPlugins = HashSet()
        invertedPlugins.add(PlayerVisibilityManager.getPluginWeakReference(plugin))
        invertedVisibilityEntities[entity.uniqueId] = invertedPlugins

        return true
    }

    private fun removeInvertedVisibility(plugin: Plugin, entity: Entity): Boolean {
        val invertedPlugins = invertedVisibilityEntities[entity.uniqueId] ?: return false // Entity isn't inverted
        invertedPlugins.remove(PlayerVisibilityManager.getPluginWeakReference(plugin))
        if (!invertedPlugins.isEmpty()) {
            return false // Some other plugins still want the entity inverted
        }
        invertedVisibilityEntities.remove(entity.uniqueId)

        return true
    }
}