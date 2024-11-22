package net.perfectdreams.dreamcustomitems.items

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.adventure.lore
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class SparklyItem(val data: SparklyItemData) {
    /**
     * Creates an [ItemStack] that represents this [SparklyItem]
     */
    fun createItemStack(): ItemStack {
        return ItemStack.of(data.material)
            .meta<ItemMeta> {
                val customModelData = data.customModelData
                if (customModelData != null)
                    setCustomModelData(customModelData)

                val rawDisplayName = data.displayName
                if (rawDisplayName != null) {
                    displayNameWithoutDecorations {
                        append(MiniMessage.miniMessage().deserialize(rawDisplayName))
                    }
                }

                val rawLore = data.lore
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
            }
    }
}