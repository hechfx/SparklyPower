package net.perfectdreams.dreamcustomitems.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.commands.options.buildSuggestionsBlockFromList
import net.perfectdreams.dreamcustomitems.items.SparklyItemsRegistry
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class CustomItemsGiveRegistryExecutor : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val itemName = word("item_name", buildSuggestionsBlockFromList {
            SparklyItemsRegistry.items.map { it.key }
        })
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val itemName = args[options.itemName]

        val sparklyItem = SparklyItemsRegistry.getItemById(itemName.lowercase())

        player.inventory.addItem(sparklyItem.createItemStack())

        player.sendMessage("§aProntinho!")
    }
}