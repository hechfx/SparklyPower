package net.perfectdreams.dreamcustomitems.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.commands.options.buildSuggestionsBlockFromList
import net.perfectdreams.dreamcustomitems.items.SparklyItemsRegistry
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class HatExecutor : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val item = player.inventory.itemInMainHand

        val type = item.type

        var allowed = false

        if (player.hasPermission("dreammini.hat")) {
            allowed = true
        } else {
            SparklyItemsRegistry.canEquipAsHat(item)
        }

        if (allowed) {
            player.inventory.setItemInMainHand(player.inventory.helmet)

            player.inventory.helmet = item

            if (type != Material.AIR) {
                player.sendMessage("§a(ﾉ ≧ ∀ ≦)ﾉ Adorei seu novo look!")
            } else {
                player.sendMessage("§aVocê consegue novamente sentir o vento soprar sua cabeça! ヽ(･ˇ ∀ˇ･ゞ)")
            }
        } else {
            player.sendMessage("§cVocê não pode colocar este item na sua cabeça!")
        }
    }
}