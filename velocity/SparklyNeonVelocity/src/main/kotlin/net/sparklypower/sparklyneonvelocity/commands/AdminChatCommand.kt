package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.format.NamedTextColor
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandArguments
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandContext
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.sparklyCommand
import net.sparklypower.sparklyvelocitycore.utils.commands.executors.SparklyCommandExecutor
import net.sparklypower.sparklyvelocitycore.utils.commands.options.CommandOptions

class AdminChatCommand(val m: SparklyNeonVelocity, val server: ProxyServer) : SparklyCommandDeclarationWrapper {
    companion object {
        val adminChatColor = NamedTextColor.AQUA
    }

    override fun declaration() = sparklyCommand(listOf("adminchat", "a", "ademirchat", "adminxet")) {
        permission = "sparklyneonvelocity.adminchat"
        executor = AdminChatExecutor(m)
    }

    class AdminChatExecutor(val m: SparklyNeonVelocity) : SparklyCommandExecutor() {
        class Options : CommandOptions() {
            val message = greedyString("message")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val message = args[options.message]
            val sender = context.requirePlayer()

            val arg0 = message.substringBefore(" ")

            if (message.isEmpty()) {
                sender.sendMessage("§cVocê não pode enviar uma mensagem vazia.".fromLegacySectionToTextComponent())
            } else if (arg0 == "lock") {
                val isLocked = m.lockedAdminChat.contains(sender.uniqueId)
                if (isLocked) {
                    m.lockedAdminChat.remove(sender.uniqueId)
                } else {
                    m.lockedAdminChat.add(sender.uniqueId)
                }

                sender.sendMessage("§x§8§3§9§E§F§7Seu chat foi ${if (isLocked) "des" else ""}travado com sucesso.".fromLegacySectionToTextComponent())
            } else {
                m.broadcastAdminChatMessage(sender, message)
            }
        }
    }
}