package net.perfectdreams.dreamcustomitems.items

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.adventure.lore
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.set
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import kotlin.reflect.KProperty1

class SparklyItem(val data: SparklyItemData) {
    /**
     * Creates an [ItemStack] that represents this [SparklyItem]
     */
    fun createItemStack(): ItemStack {
        val rawMaterial = getAttributeCheckParents(SparklyItemData::material) ?: error("Missing Material for item ${data.id}! You can't create a ItemStack without a Material!")

        return ItemStack.of(rawMaterial)
            .meta<ItemMeta> {
                persistentDataContainer.set(SparklyItemsRegistry.SPARKLYPOWER_CUSTOM_ITEM_ID, data.id)

                setRarity(getAttributeCheckParents(SparklyItemData::rarity))

                val customModelData = getAttributeCheckParents(SparklyItemData::customModelData)
                if (customModelData != null)
                    setCustomModelData(customModelData)

                val rawItemName = getAttributeCheckParents(SparklyItemData::itemName)
                if (rawItemName != null) {
                    itemName(MiniMessage.miniMessage().deserialize(rawItemName))
                }

                val rawDisplayName = getAttributeCheckParents(SparklyItemData::displayName)
                if (rawDisplayName != null) {
                    displayNameWithoutDecorations {
                        append(MiniMessage.miniMessage().deserialize(rawDisplayName))
                    }
                }

                val rawLore = getAttributeCheckParents(SparklyItemData::lore)
                if (rawLore != null) {
                    lore {
                        for (line in rawLore.map { MiniMessage.miniMessage().deserialize(it) }) {
                            this.textWithoutDecorations {
                                color(NamedTextColor.GRAY)
                                append(line)
                            }
                        }
                    }
                }

                val rawJukeboxPlayable = getAttributeCheckParents(SparklyItemData::jukeboxPlayable)
                if (rawJukeboxPlayable != null) {
                    setJukeboxPlayable(
                        jukeboxPlayable.apply {
                            this.songKey = NamespacedKey.fromString(rawJukeboxPlayable.songKey)!!
                        }
                    )
                }
            }
    }

    /**
     * Gets the value of the [kProperty1], checking the item [SparklyItemData] and all of its parents
     */
    fun <T> getAttributeCheckParents(kProperty1: KProperty1<SparklyItemData, T>): T? {
        val dataToBeChecked = mutableListOf<SparklyItemData>()

        fun add(data: SparklyItemData) {
            dataToBeChecked.add(data)

            if (data.parent != null) {
                add(SparklyItemsRegistry.getItemById(data.parent).data)
            }
        }

        add(data)

        for (data in dataToBeChecked) {
            val value = kProperty1.get(data)
            if (value != null)
                return value
        }

        return null
    }
}