package net.perfectdreams.dreamcustomitems.listeners

import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.extensions.canPlaceAt
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcore.utils.get
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreamcustomitems.items.SparklyItemData
import net.perfectdreams.dreamcustomitems.items.SparklyItemsRegistry
import net.perfectdreams.dreamcustomitems.paintings.SparklyPaintingsRegistry
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.CraftRegistry
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.Painting
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * A listener that mimicks Minecraft's Vanilla Painting behavior, used for custom paintings that are given out as items
 */
class CustomPaintingListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onPainting(event: PlayerInteractEvent) {
        val heldItem = event.item ?: return
        val clickedBlock = event.clickedBlock ?: return

        if (event.rightClick && event.action == Action.RIGHT_CLICK_BLOCK) {
            val sparklyItem = SparklyItemsRegistry.getMatchedItem(heldItem) ?: return

            val hangablePainting = sparklyItem.getAttributeCheckParents(SparklyItemData::hangablePainting) ?: return

            // Okay, so it is a custom hangable painting!
            event.isCancelled = true

            fun getOffsetForBlockFace(face: BlockFace): Triple<Double, Double, Double> {
                return when (face) {
                    BlockFace.NORTH -> Triple(0.0, 0.0, -1.0)
                    BlockFace.EAST -> Triple(1.0, 0.0, 0.0)
                    BlockFace.SOUTH -> Triple(0.0, 0.0, 1.0)
                    BlockFace.WEST -> Triple(-1.0, 0.0, 0.0)
                    BlockFace.UP -> Triple(0.0, 1.0, 0.0)
                    BlockFace.DOWN -> Triple(0.0, -1.0, 0.0)
                    else -> Triple(0.0, 0.0, 0.0) // Default case, just in case
                }
            }

            fun applyBlockFaceOffset(location: Location, face: BlockFace): Location {
                val (x, y, z) = getOffsetForBlockFace(face)
                return location.clone().add(x, y, z)
            }

            val targetDirection = when (event.blockFace) {
                BlockFace.NORTH -> Direction.NORTH
                BlockFace.EAST ->  Direction.EAST
                BlockFace.SOUTH ->  Direction.SOUTH
                BlockFace.WEST ->  Direction.WEST
                BlockFace.UP ->  Direction.UP
                BlockFace.DOWN -> Direction.DOWN
                else -> error("Unsupported face")
            }

            val properPaintingLocation = applyBlockFaceOffset(clickedBlock.location, event.blockFace)
            if (!event.player.canPlaceAt(clickedBlock.location, Material.PAINTING)) {
                event.player.sendMessage(
                    textComponent {
                        color(NamedTextColor.RED)
                        content("Você não tem permissão para colocar o quadro aqui!")
                    }
                )
                return
            }

            val paintingVariantHolder = SparklyPaintingsRegistry.getPaintingVariantHolderById(hangablePainting.paintingKey) ?: error("Painting Variant \"${hangablePainting.paintingKey}\" does not exist! Bug?")

            val world = clickedBlock.world
            val nmsWorld = (world as CraftWorld).handle
            val nmsPainting = net.minecraft.world.entity.decoration.Painting(
                nmsWorld,
                BlockPos(properPaintingLocation.x.toInt(), properPaintingLocation.y.toInt(), properPaintingLocation.z.toInt()),
                targetDirection,
                paintingVariantHolder
            )

            if (!nmsPainting.generation && !nmsPainting.survives()) {
                event.player.sendMessage(
                    textComponent {
                        color(NamedTextColor.RED)
                        content("Não tem espaço suficiente para colocar o quadro aqui!")
                    }
                )

                // Technically we don't need to remove the painting from the world because it was never added to the world
                // nmsPainting.remove(Entity.RemovalReason.DISCARDED)
                return
            }

            nmsWorld.addFreshEntity(nmsPainting)
            val bukkitPainting = nmsPainting.bukkitEntity

            bukkitPainting.persistentDataContainer.set(SparklyPaintingsRegistry.SPARKLYPOWER_CUSTOM_PAINTING_SPAWNED_BY_ITEM_ID, sparklyItem.data.id)

            if (event.player.gameMode != GameMode.CREATIVE)
                event.item!!.amount -= 1

            world.playSound(properPaintingLocation, Sound.ENTITY_PAINTING_PLACE, 1f, 1f)
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onPaintingBreak(e: HangingBreakEvent) {
        val entity = e.entity
        if (entity is Painting) {
            val spawnedByItemId = entity.persistentDataContainer.get(SparklyPaintingsRegistry.SPARKLYPOWER_CUSTOM_PAINTING_SPAWNED_BY_ITEM_ID)
            if (spawnedByItemId != null) {
                e.isCancelled = true
                e.entity.remove()
                e.entity.world.dropItem(e.entity.location, SparklyItemsRegistry.getItemById(spawnedByItemId).createItemStack())
                e.entity.world.playSound(e.entity.location, Sound.ENTITY_PAINTING_BREAK, 1f, 1f)
            }
        }
    }
}