package net.perfectdreams.dreamcore.utils.commands.context

import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.commands.arguments.selector.EntitySelector
import net.perfectdreams.dreamcore.utils.DefaultFontInfo
import net.perfectdreams.dreamcore.utils.commands.options.CommandOption
import net.perfectdreams.dreamcore.utils.commands.options.PlayerCommandOption
import org.bukkit.entity.Player

class CommandArguments(val context: CommandContext) {
    companion object {
        val PLAYER_NOT_FOUND: () -> (Component) = {
            Component.text {
                it.color(TextColor.color(255, 0, 0))
                it.content("O jogador não existe ou está offline! Verifique se você escreveu o nome correto dele!")
            }
        }
    }

    /**
     * If the player does not exist, Minecraft will throw a parse error, that's why we verify here!
     */
    fun getAndValidate(argument: CommandOption<Player>, reason: () -> (Component) = PLAYER_NOT_FOUND): Player {
        val entitySelector = context.nmsContext.getArgument(argument.name, EntitySelector::class.java)
        try {
            return entitySelector.findSinglePlayer(context.nmsContext.source).bukkitEntity as Player
        } catch (e: CommandSyntaxException) {
            context.fail(reason.invoke())
        }
    }

    inline operator fun <reified T> get(argument: CommandOption<T>): T {
        // ===[ SPECIAL CASES ]===
        if (argument is PlayerCommandOption) {
            val entitySelector = context.nmsContext.getArgument(argument.name, EntitySelector::class.java)
            return entitySelector.findSinglePlayer(context.nmsContext.source).bukkitEntity as T // T should always be a (Craft)Player
        }

        return context.nmsContext.getArgument(argument.name, T::class.java)
    }
}