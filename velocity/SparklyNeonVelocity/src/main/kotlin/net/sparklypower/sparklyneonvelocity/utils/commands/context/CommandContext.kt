package net.sparklypower.sparklyneonvelocity.utils.commands.context

import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor

class CommandContext(val nmsContext: CommandContext<CommandSource>) {
    companion object {
        val MISSING_PERMISSIONS: () -> (Component) = {
            Component.text {
                it.color(TextColor.color(255, 0, 0))
                it.content("Você não tem permissão para fazer isso! Xô, sai daqui!!")
            }
        }
    }

    val sender = nmsContext.source

    // fun sendMessage(message: String) = sender.sendMessage(message)
    fun sendMessage(component: Component) = sender.sendMessage(component)
    // fun sendMessage(block: TextComponent.Builder.() -> (Unit) = {}) = sendMessage(textComponent(block))

    /**
     * Requires that the [sender] is a Player. If it isn't, the command will [fail].
     */
    /* fun requirePlayer(): Player {
        val s = sender
        if (s !is Player)
            fail(
                Component.text {
                    it.color(TextColor.color(255, 0, 0))
                    it.content("Apenas jogadores podem executar este comando!")
                }
            )
        return s
    } */

    /**
     * Requires that the [sender] is a Console. If it isn't, the command will [fail]~.
     */
    /* fun requireConsole(): ConsoleCommandSource {
        val s = sender
        if (s !is ConsoleCommandSource)
            fail(
                Component.text {
                    it.color(TextColor.color(255, 0, 0))
                    it.content("Apenas o console pode executar este comando!")
                }
            )
        return s
    } */

    /**
     * Requires that the [sender] has all the required [permissions]. If it isn't, the command will [fail].
     */
    fun requirePermissions(vararg permissions: String, reason: () -> (Component) = MISSING_PERMISSIONS): Boolean {
        TODO()
        /* val s = sender
        val hasAllPermissions = permissions.all { s.hasPermission(it) }
        if (!hasAllPermissions) {

        }
            fail(reason.invoke())
        return hasAllPermissions */
    }

    /* fun fail(message: String): Nothing = fail(
        net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
            .deserialize(message)
    )

    fun fail(component: Component): Nothing = throw CommandException(component) */
}