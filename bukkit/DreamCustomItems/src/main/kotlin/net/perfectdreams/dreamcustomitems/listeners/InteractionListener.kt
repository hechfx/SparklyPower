package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcustomitems.items.SparklyItemData
import net.perfectdreams.dreamcustomitems.items.SparklyItemsRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class InteractionListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        if (!e.player.isSneaking)
            return

        val item = e.item ?: return
        val sparklyItem = SparklyItemsRegistry.getMatchedItem(item) ?: return
        val itemAppearanceShiftChanger = sparklyItem.getAttributeCheckParents(SparklyItemData::itemAppearanceShiftChanger)
        if (itemAppearanceShiftChanger != null && item.hasItemMeta()) {
            val currentCustomModelData = item.itemMeta.customModelData
            val currentModelIndex = itemAppearanceShiftChanger.modelIds.indexOf(currentCustomModelData)
            val newModelIndex = ((currentModelIndex + 1) % itemAppearanceShiftChanger.modelIds.size)

            item.editMeta {
                it.setCustomModelData(itemAppearanceShiftChanger.modelIds[newModelIndex])
            }

            val soundKey = itemAppearanceShiftChanger.soundKey
            if (soundKey != null)
                e.player.playSound(e.player, soundKey, 1f, 1f)
        }
    }
}