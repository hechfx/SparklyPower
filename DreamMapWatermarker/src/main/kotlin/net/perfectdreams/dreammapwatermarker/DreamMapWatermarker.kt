package net.perfectdreams.dreammapwatermarker

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemFlag
import java.util.*

class DreamMapWatermarker : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(this)

		registerCommand(
			command("DreamWatermarkMap", listOf("watermarkmap")) {
				permission = "dreamwatermarkmap.watermark"

				executes {
					val playerName = args.getOrNull(0) ?: run {
						player.sendMessage("§cVocê precisa colocar o nome do player!")
						return@executes
					}

					val uniqueId = UUID.nameUUIDFromBytes("OfflinePlayer:$playerName".toByteArray())

					val item = player.inventory.itemInMainHand

					player.inventory.setItemInMainHand(
						item.lore(
							"§7Diretamente de §dXerox da Pantufa§7...",
							"§7(temos os melhores preços da região!)",
							"§7§oUm incrível mapa para você!",
							"§7",
							"§7Mapa feito para §a${playerName} §e(◠‿◠✿)"
						).apply {
							this.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
							this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
						}.storeMetadata("customMapOwner", uniqueId.toString())
					)
				}
			}
		)
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun craft(event: InventoryClickEvent) {
		// We could use "clickedInventory" but that does disallow dragging from the bottom to the top
		val clickedInventory = event.whoClicked.openInventory
		val currentItem = event.currentItem ?: return

		if (clickedInventory.type != InventoryType.CARTOGRAPHY) // el gambiarra
			return

		if (currentItem.getStoredMetadata("customMapOwner") != null || currentItem.lore?.lastOrNull() == "§a§lObrigado por votar! ^-^") {
			event.isCancelled = true
		}

		// Bukkit.broadcastMessage("Moveu item ${event.destination}")
	}
}