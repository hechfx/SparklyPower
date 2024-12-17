package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.title.Title
import net.sparklypower.common.utils.convertToEpochMillisRelativeToNow
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.PunishmentManager
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.Ban
import net.sparklypower.sparklyneonvelocity.dao.ConnectionLogEntry
import net.sparklypower.sparklyneonvelocity.dao.IpBan
import net.sparklypower.sparklyneonvelocity.dao.User
import net.sparklypower.sparklyneonvelocity.tables.ConnectionLogEntries
import net.sparklypower.sparklyneonvelocity.tables.PremiumUsers
import net.sparklypower.sparklyneonvelocity.utils.socket.SocketServer
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandArguments
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandContext
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclaration
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.sparklyCommand
import net.sparklypower.sparklyvelocitycore.utils.commands.executors.SparklyCommandExecutor
import net.sparklypower.sparklyvelocitycore.utils.commands.options.CommandOptions
import net.sparklypower.sparklyvelocitycore.utils.commands.options.buildSuggestionsBlockFromList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.jvm.optionals.getOrNull

class BanCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("ban")) {
        permission = "sparklyneonvelocity.ban"
        executor = BanExecutor(m, server)
    }

    class BanExecutor(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val playerName = word(
                "player_name",
                buildSuggestionsBlockFromList {
                    server.allPlayers.map { it.username }
                }
            )

            val reason = optionalGreedyString("reason")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val playerName = args[options.playerName]
            val sender = context.sender

            val reason =  args[options.reason]?.ifEmpty { null }

            val (punishedDisplayName, punishedUniqueId, player) = m.punishmentManager.getPunishedInfoByString(playerName) ?: run {
                context.sendMessage("§cEu sei que você tá correndo para banir aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".fromLegacySectionToTextComponent())
                return
            }

            var effectiveReason = reason ?: "Sem motivo definido"

            var silent = false
            if (effectiveReason.contains("-s")) {
                silent = true

                effectiveReason = effectiveReason.replace("-s", "")
            }

            var ipBan = false
            if (effectiveReason.contains("-i")) {
                ipBan = true

                effectiveReason = effectiveReason.replace("-i", "")
            }

            var temporary = false
            var time = 0.toLong()
            if (effectiveReason.contains("-t")) {
                temporary = true

                val splitted = effectiveReason.split("-t")
                val timeSpec = splitted[1]

                val timeMillis = timeSpec.convertToEpochMillisRelativeToNow()
                if (timeMillis <= System.currentTimeMillis()) { // :rolling_eyes:
                    context.sendMessage("§cNão sei se você está congelado no passado, mas o tempo que você passou está no passado! o.O".fromLegacySectionToTextComponent())
                    return
                }

                effectiveReason = effectiveReason.replace("-t$timeSpec", "")
                time = timeMillis
            }

            val punisherDisplayName = m.punishmentManager.getPunisherName(context.sender)

            val geoLocalization = m.pudding.transactionBlocking {
                ConnectionLogEntry.find { ConnectionLogEntries.player eq punishedUniqueId!! }.maxByOrNull { it.connectedAt }
            }

            val ip = if (player != null)
                player.remoteAddress.hostString
            else
                geoLocalization?.ip

            m.pudding.transactionBlocking {
                if (ipBan) {
                    if (ip == null) {
                        context.sendMessage("§cInfelizmente não há nenhum registro de IP do player §e$punishedDisplayName§c!".fromLegacySectionToTextComponent())
                        return@transactionBlocking
                    }

                    IpBan.new {
                        this.ip = ip

                        this.punishedBy = when (sender) {
                            is Player -> sender.uniqueId
                            is SocketServer.FakeCommandPlayerSender -> sender.uniqueId
                            else -> null
                        }
                        this.punishedAt = System.currentTimeMillis()
                        this.reason = effectiveReason

                        if (temporary) {
                            this.temporary = true
                            this.expiresAt = time
                        }
                    }
                }

                Ban.new {
                    this.player = punishedUniqueId!!
                    this.punishedBy = when (sender) {
                        is Player -> sender.uniqueId
                        is SocketServer.FakeCommandPlayerSender -> sender.uniqueId
                        else -> null
                    }
                    this.punishedAt = System.currentTimeMillis()
                    this.reason = effectiveReason

                    if (temporary) {
                        this.temporary = true
                        this.expiresAt = time
                    }
                }
            }

            if (ip != null && !ipBan && !temporary) {
                m.pudding.transactionBlocking {
                    IpBan.new {
                        this.ip = ip

                        this.punishedBy = when (sender) {
                            is Player -> sender.uniqueId
                            is SocketServer.FakeCommandPlayerSender -> sender.uniqueId
                            else -> null
                        }
                        this.punishedAt = System.currentTimeMillis()
                        this.reason = effectiveReason
                        this.temporary = true
                        this.expiresAt = System.currentTimeMillis() + PunishmentManager.DEFAULT_IPBAN_EXPIRATION
                    }
                }
            }

            // Vamos expulsar o player ao ser banido
            player?.disconnect("""
			§cVocê foi banido!
			§cMotivo:

			§a$effectiveReason
			§cPor: $punisherDisplayName
        """.trimIndent().fromLegacySectionToTextComponent())

            context.sendMessage("§b${punishedDisplayName}§a foi punido com sucesso, yay!! ^-^".fromLegacySectionToTextComponent())

            m.punishmentManager.sendPunishmentToDiscord(
                silent,
                punishedDisplayName ?: "Nome desconhecido",
                punishedUniqueId!!,
                "Banido ${if (temporary) "Temporariamente" else "Permanentemente"}",
                punisherDisplayName,
                effectiveReason,
                (context.sender as? Player)?.currentServer?.getOrNull()?.server?.serverInfo?.name,
                null
            )

            if (!silent) {
                server.sendMessage("§b${punisherDisplayName}§a baniu §c${punishedDisplayName}§a por §6\"§e${effectiveReason}§6\"§a!".fromLegacySectionToTextComponent())
            }
        }
    }
}