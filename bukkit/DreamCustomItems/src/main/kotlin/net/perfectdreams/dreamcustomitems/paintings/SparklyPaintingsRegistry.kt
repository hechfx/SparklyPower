package net.perfectdreams.dreamcustomitems.paintings

import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.decoration.PaintingVariant
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKeyWithType
import org.bukkit.craftbukkit.CraftRegistry
import org.bukkit.persistence.PersistentDataType
import kotlin.jvm.optionals.getOrNull

object SparklyPaintingsRegistry {
    val SPARKLYPOWER_CUSTOM_PAINTING_ID = SparklyNamespacedKeyWithType(SparklyNamespacedKey("sparklypower_custom_painting_id"), PersistentDataType.STRING)
    val SPARKLYPOWER_CUSTOM_PAINTING_SPAWNED_BY_ITEM_ID = SparklyNamespacedKeyWithType(SparklyNamespacedKey("sparklypower_custom_painting_spawned_by_item_id"), PersistentDataType.STRING)

    fun getPaintingVariantById(id: String): PaintingVariant? {
        val registry = CraftRegistry.getMinecraftRegistry(Registries.PAINTING_VARIANT)
        val paintingVariant = registry.get(ResourceLocation.parse(id))

        return paintingVariant.getOrNull()?.value()
    }

    fun getPaintingVariantHolderById(id: String): Holder<PaintingVariant>? {
        val registry = CraftRegistry.getMinecraftRegistry(Registries.PAINTING_VARIANT)
        val paintingVariant = registry.get(ResourceLocation.parse(id)).getOrNull() ?: return null

        return paintingVariant
    }
}