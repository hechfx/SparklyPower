package net.perfectdreams.dreamcustomitems.items

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.io.File

object SparklyItemsRegistry {
    val items = mutableMapOf<String, SparklyItem>()

    fun getItemById(id: String): SparklyItem {
        return items[id]!!
    }

    /**
     * Reloads the SparklyPower item registry
     */
    fun reload(m: DreamCustomItems) {
        // Clear all items
        items.clear()

        val sparklyItems = Yaml.default.decodeFromString<List<SparklyItemData>>(File(m.dataFolder, "sparkly_items.yml").readText(Charsets.UTF_8))
        for (item in sparklyItems) {
            items[item.id] = SparklyItem(item)
        }
    }

    /**
     * Checks if an item can be equipped as a hat
     */
    fun canEquipAsHat(cursor: ItemStack): Boolean {
        for (item in items) {
            if (item.value.data.equippable?.slot == EquipmentSlot.HEAD && cursor.type == cursor.type && cursor.hasItemMeta()) {
                val meta = cursor.itemMeta
                if (!meta.hasCustomModelData())
                    continue

                if (meta.customModelData == item.value.data.customModelData)
                    return true
            }
        }

        // Items that are not registered as a SparklyPower Custom Item
        if (cursor.type != Material.PAPER)
            return false

        val meta = cursor.itemMeta
        if (!meta.hasCustomModelData())
            return false

        return meta.customModelData in 133..169 || meta.customModelData == 197 || meta.customModelData in 207..214
    }
}