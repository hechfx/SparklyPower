package net.perfectdreams.dreamcustomitems.items

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKeyWithType
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File

object SparklyItemsRegistry {
    val items = mutableMapOf<String, SparklyItem>()
    val SPARKLYPOWER_CUSTOM_ITEM_ID = SparklyNamespacedKeyWithType(SparklyNamespacedKey("sparklypower_custom_item_id"), PersistentDataType.STRING)

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
     * Gets which item the [cursor] matches in the [SparklyItemsRegistry] item registry
     */
    fun getMatchedItem(cursor: ItemStack): SparklyItem? {
        for (item in items) {
            if (cursor.persistentDataContainer.get(SPARKLYPOWER_CUSTOM_ITEM_ID) == item.key) {
                return item.value
            }
        }

        return null
    }

    /**
     * Checks if an item can be equipped as a hat
     */
    fun canEquipAsHat(cursor: ItemStack): Boolean {
        for (item in items) {
            if (cursor.persistentDataContainer.get(SPARKLYPOWER_CUSTOM_ITEM_ID) == item.key) {
                if (item.value.data.equippable?.slot == EquipmentSlot.HEAD) {
                    return true
                }
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