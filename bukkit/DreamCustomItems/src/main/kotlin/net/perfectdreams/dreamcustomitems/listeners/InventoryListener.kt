package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.items.SparklyItemsRegistry
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class InventoryListener(val m: DreamCustomItems) : Listener {
    // FROM MASSIVEHAT

    // -------------------------------------------- //
    // CONSTANTS
    // -------------------------------------------- //
    var RAW_HAT_SLOT_ID = 5

    // -------------------------------------------- //
    // LISTENER
    // -------------------------------------------- //
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun hatSwitch(event: InventoryClickEvent) {
        val clicker: HumanEntity = event.whoClicked

        // If a player ...
        if (clicker !is Player) return
        val me: Player = clicker
        val view: InventoryView = event.view

        // ... is clicking around in their own/armor/crafting view ...
        if (view.type !== InventoryType.CRAFTING) return

        // ... and they are clicking their hat slot ...
        if (event.rawSlot != RAW_HAT_SLOT_ID) return
        val cursor: ItemStack = event.cursor

        val canEquip = SparklyItemsRegistry.canEquipAsHat(cursor)

        if (canEquip) {
            // ... then perform the switch.
            // We deny the normal result
            // NOTE: There is no need to cancel the event since that is just a proxy method for the line below.
            event.result = Event.Result.DENY

            val current: ItemStack? = event.currentItem

            // Set
            event.currentItem = cursor
            view.setCursor(current)

            // Update
            clicker.updateInventory()
        }
    }
}