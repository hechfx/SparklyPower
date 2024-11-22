package net.perfectdreams.dreamcustomitems.items

import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot

@Serializable
data class SparklyItemData(
    val id: String,
    val material: Material,
    val customModelData: Int? = null,
    val displayName: String? = null,
    val lore: List<String>? = null,
    val equippable: Equippable? = null
) {
    @Serializable
    data class Equippable(
        val slot: EquipmentSlot
    )
}