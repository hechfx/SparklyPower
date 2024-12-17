package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.Ban
import net.sparklypower.sparklyneonvelocity.dao.ConnectionLogEntry
import net.sparklypower.sparklyneonvelocity.dao.IpBan
import net.sparklypower.sparklyneonvelocity.dao.User
import net.sparklypower.sparklyneonvelocity.tables.Bans
import net.sparklypower.sparklyneonvelocity.tables.ConnectionLogEntries
import net.sparklypower.sparklyneonvelocity.tables.IpBans
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandArguments
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandContext
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclaration
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.sparklyCommand
import net.sparklypower.sparklyvelocitycore.utils.commands.executors.SparklyCommandExecutor
import net.sparklypower.sparklyvelocitycore.utils.commands.options.CommandOptions
import net.sparklypower.sparklyvelocitycore.utils.commands.options.buildSuggestionsBlockFromList
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import java.util.*
import kotlin.jvm.optionals.getOrNull

class DupeIpCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dupeip")) {
        permission = "sparklyneonvelocity.dupeip"
        executor = DupeIpExecutor(m, server)
    }

    class DupeIpExecutor(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val playerName = word(
                "player_name",
                buildSuggestionsBlockFromList {
                    server.allPlayers.map { it.username }
                }
            )
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val playerName = args[options.playerName]

            // Primeiramente vamos pegar o UUID para achar o IP
            val playerUniqueId = try {
                UUID.fromString(playerName)
            } catch (e: IllegalArgumentException) {
                m.punishmentManager.getUniqueId(playerName)
            }

            // Vamos pegar o player
            val playerLastConnection = m.pudding.transactionBlocking {
                ConnectionLogEntry.find { (ConnectionLogEntries.player eq playerUniqueId) or (ConnectionLogEntries.ip eq playerName) }
                    .maxByOrNull { it.connectedAt }
            }

            if (playerLastConnection == null) {
                context.sendMessage("§cNão achei nenhum Player com esse nome!".fromLegacySectionToTextComponent())
                return
            }

            val ip = playerLastConnection.ip

            val ipBan = m.pudding.transactionBlocking {
                IpBan.find {
                    (IpBans.ip eq ip) and (IpBans.temporary eq false or (IpBans.temporary eq true and IpBans.expiresAt.greaterEq(
                        System.currentTimeMillis()
                    )))
                }.firstOrNull()
            }

            val asn = m.asnManager.getAsnForIP(ip)

            // Caso achar...
            context.sendMessage("Escaneando ${if (ipBan != null) "§c" else "§f"}$ip §a(${asn?.first}, ${asn?.second?.name})".fromLegacySectionToTextComponent())

            // Agora vamos achar todos os players que tem o mesmo IP
            val connectionLogEntries = m.pudding.transactionBlocking {
                ConnectionLogEntry.find {
                    ConnectionLogEntries.ip eq playerLastConnection.ip
                }.toList()
            }

            // And now we are going to try getting all players that also have the same latest IP
            val connectionLogEntriesAndLastIPs = m.pudding.transactionBlocking {
                ConnectionLogEntry.find {
                    ConnectionLogEntries.player inList connectionLogEntries.map { it.player }
                }.orderBy(ConnectionLogEntries.connectedAt to SortOrder.DESC)
                    .limit(1)
                    .toList()
                    .filter {
                        it.ip == playerLastConnection.ip
                    }
            }

            val onlyMatchingAnyIP = connectionLogEntries.filterNot { entry ->
                connectionLogEntriesAndLastIPs.any {
                    it.player == entry.player
                }
            }

            val uniqueIds = connectionLogEntriesAndLastIPs.distinctBy { it.player }.map { it.player }
            val anyIPUniqueIds = onlyMatchingAnyIP.distinctBy { it.player }.map { it.player }

            val matchingLastIPsAccounts = uniqueIds.joinToString(", ", transform = {
                // Está banido?
                val ban = m.pudding.transactionBlocking {
                    Ban.find {
                        (Bans.player eq it) and (Bans.temporary eq false or (Bans.temporary eq true and Bans.expiresAt.greaterEq(
                            System.currentTimeMillis()
                        )))
                    }.firstOrNull()
                }

                // Se ele estiver banido...
                if (ban != null) {
                    val punishedName = m.pudding.transactionBlocking { User.findById(ban.player) }

                    return@joinToString "§c${punishedName?.username}"
                }

                // Está online?
                val isOnline = server.getPlayer(it).getOrNull()
                if (isOnline != null && isOnline.isActive) {
                    // Sim ele está online
                    val onlineName = m.pudding.transactionBlocking { User.findById(it) }

                    return@joinToString "§a${onlineName?.username}"
                } else {
                    // Ele não está online
                    val offlineName = m.pudding.transactionBlocking { User.findById(it) }

                    return@joinToString "§7${offlineName?.username}"
                }
            })

            val matchingAnyIPsAccounts = anyIPUniqueIds.joinToString(", ", transform = {
                // Está banido?
                val ban = m.pudding.transactionBlocking {
                    Ban.find {
                        (Bans.player eq it) and (Bans.temporary eq false or (Bans.temporary eq true and Bans.expiresAt.greaterEq(
                            System.currentTimeMillis()
                        )))
                    }.firstOrNull()
                }

                // Se ele estiver banido...
                if (ban != null) {
                    val punishedName = m.pudding.transactionBlocking { User.findById(ban.player) }

                    return@joinToString "§c${punishedName?.username}"
                }

                // Está online?
                val isOnline = server.getPlayer(it).getOrNull()
                if (isOnline != null && isOnline.isActive) {
                    // Sim ele está online
                    val onlineName = m.pudding.transactionBlocking { User.findById(it) }

                    return@joinToString "§a${onlineName?.username}"
                } else {
                    // Ele não está online
                    val offlineName = m.pudding.transactionBlocking { User.findById(it) }

                    return@joinToString "§7${offlineName?.username}"
                }
            })

            // Mandar o resultado final
            context.sendMessage("[§cBanidos§f] [§aOnline§f] [§7Offline§f]".fromLegacySectionToTextComponent())
            context.sendMessage("§bContas que usaram o mesmo IP na última entrada ao servidor: $matchingLastIPsAccounts".fromLegacySectionToTextComponent())
            if (matchingAnyIPsAccounts.isNotEmpty()) {
                context.sendMessage("§bContas que usaram o mesmo IP alguma vez na vida: $matchingAnyIPsAccounts".fromLegacySectionToTextComponent())
            }
            context.sendMessage("§7Para mais informações, use §6/advdupeip".fromLegacySectionToTextComponent())
        }
    }
}