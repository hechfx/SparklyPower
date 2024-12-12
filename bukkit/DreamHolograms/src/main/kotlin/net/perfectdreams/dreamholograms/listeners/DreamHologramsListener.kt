package net.perfectdreams.dreamholograms.listeners

import net.perfectdreams.dreamholograms.DreamHolograms
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent

class DreamHologramsListener(val m: DreamHolograms) : Listener {
    // the EntitiesLoadEvent
    // but yes, the block chunk may not be loaded
    // blocks and entity chunks are separate, and there is no guarantee for the order in which they will load
    // https://canary.discord.com/channels/289587909051416579/555462289851940864/1316430545885991002
    @EventHandler
    fun onEntitiesLoad(e: EntitiesLoadEvent) {
        for (hologram in m.holograms) {
            val isInChunk = hologram.value.isInChunk(e.chunk)

            if (isInChunk) {
                hologram.value.spawnHologram()
            }
        }
    }
}