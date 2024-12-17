package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.utils.ASNManager
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandArguments
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandContext
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclaration
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.sparklyCommand
import net.sparklypower.sparklyvelocitycore.utils.commands.executors.SparklyCommandExecutor
import kotlin.jvm.optionals.getOrNull

class CheckAndKickCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("checkandkick")) {
        permission = "sparklyneonvelocity.checkandkick"
        executor = CheckAndKickExecutor(m, server)
    }

    class CheckAndKickExecutor(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            context.sendMessage("§aVerificando players online que deveriam estar banidos... (IP ban ou ASN ban)".fromLegacySectionToTextComponent())

            for (player in server.allPlayers) {
                val result = runBlocking { m.asnManager.isAsnBlacklisted(player.remoteAddress.hostString) }

                if (result.blocked) {
                    context.sendMessage("§eExpulsando ${player.username} pois o ASN está bloqueado!".fromLegacySectionToTextComponent())
                    player.disconnect("""§cSeu IP está bloqueado, desative VPNs ou proxies ativos para poder jogar!"""".trimMargin().fromLegacySectionToTextComponent())
                }
            }

            context.sendMessage("§aVerificação concluída!".fromLegacySectionToTextComponent())
        }
    }
}