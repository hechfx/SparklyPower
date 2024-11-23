package net.perfectdreams.dreamcustomitems.items

import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemRarity

@Serializable
data class SparklyItemData(
    val id: String,
    val parent: String? = null,
    val material: Material? = null,
    val rarity: ItemRarity? = null,
    val customModelData: Int? = null,
    val itemName: String? = null,
    val displayName: String? = null,
    val lore: List<String>? = null,
    val equippable: Equippable? = null,
    val jukeboxPlayable: JukeboxPlayable? = null,
    val itemAppearanceShiftChanger: ItemAppearanceShiftChanger? = null
) {
    @Serializable
    data class Equippable(
        val slot: EquipmentSlot
    )

    @Serializable
    data class JukeboxPlayable(
        val songKey: String
    )

    @Serializable
    data class ItemAppearanceShiftChanger(
        val modelIds: List<Int>,
        val soundKey: String?
    )
}