package net.perfectdreams.dreamcustomitems.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.commands.options.buildSuggestionsBlockFromList
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.items.SparklyItemsRegistry
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class CustomItemsReloadRegistryExecutor(val m: DreamCustomItems) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        SparklyItemsRegistry.reload(m)

        player.sendMessage("§aProntinho!")
    }
}