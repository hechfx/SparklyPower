package net.sparklypower.sparklyneonvelocity.network.processors

import com.velocitypowered.api.proxy.ProxyServer
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.luckperms.api.LuckPermsProvider
import net.sparklypower.common.utils.adventure.TextComponent
import net.sparklypower.rpc.proxy.ProxySendAdminChatRequest
import net.sparklypower.rpc.proxy.ProxySendAdminChatResponse
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.commands.AdminChatCommand
import net.sparklypower.sparklyneonvelocity.dao.User
import net.sparklypower.sparklyneonvelocity.network.RPCProcessor
import net.sparklypower.sparklyneonvelocity.utils.StaffColors
import net.sparklypower.sparklyneonvelocity.utils.emotes

class ProxySendAdminChatProcessor(val m: SparklyNeonVelocity, val server: ProxyServer) : RPCProcessor<ProxySendAdminChatRequest, ProxySendAdminChatResponse> {
    override fun process(
        call: ApplicationCall,
        request: ProxySendAdminChatRequest
    ): ProxySendAdminChatResponse {
        val additionalRawJsonMessageJavaOnlyClient = request.additionalRawJsonMessageJavaOnlyClient?.let { JSONComponentSerializer.json().deserialize(it) }

        server.allPlayers
            .filter { it.hasPermission("sparklyneonvelocity.adminchat") && it.uniqueId in m.loggedInPlayers }
            .forEach {
                // We create the message here because we need to check if it is a geyser client before attempting to send a bunch of """images"""
                val isGeyser = m.isGeyser(it)

                val message = when (val adminChatSender = request.sender) {
                    is ProxySendAdminChatRequest.AdminChatSender.SparklyUser -> {
                        val lpUser = LuckPermsProvider.get().userManager.loadUser(adminChatSender.playerUniqueId).join()

                        val user = runBlocking {
                            m.pudding.transaction {
                                User.findById(adminChatSender.playerUniqueId)
                            }
                        }

                        val primaryGroup = lpUser.primaryGroup

                        // The last color is a fallback, it checks for "group.default", so everyone should, hopefully, have that permission
                        val role = StaffColors.entries.firstOrNull { it.permission == "group.$primaryGroup" } ?: StaffColors.DEFAULT

                        val isGirl = runBlocking {
                            m.pudding.transaction {
                                User.findById(adminChatSender.playerUniqueId)?.isGirl ?: false
                            }
                        }

                        val colors = role.colors
                        val prefix = with (role.prefixes) { if (isGirl && size == 2) get(1) else get(0) }
                        val emote = emotes[user?.username]?.toString() ?: "" // This sucks, it would be better to map UUIDs to emotes, not usernames...

                        TextComponent {
                            append(TextComponent("$prefix \uE23C"))
                            append(TextComponent(" "))
                            if (emote != null) {
                                append(
                                    TextComponent {
                                        content("$emote ")
                                    }
                                )
                            }

                            append(
                                TextComponent {
                                    content(user?.username ?: "???")
                                    color(colors.nick)
                                }
                            )

                            append(
                                TextComponent {
                                    content(": ")
                                    color(AdminChatCommand.adminChatColor)

                                    append(
                                        TextComponent {
                                            append(JSONComponentSerializer.json().deserialize(request.rawJsonMessage))
                                        }
                                    )
                                }
                            )

                            if (!isGeyser && additionalRawJsonMessageJavaOnlyClient != null) {
                                appendNewline()
                                append(additionalRawJsonMessageJavaOnlyClient)
                            }
                        }
                    }
                    is ProxySendAdminChatRequest.AdminChatSender.UnknownUser -> {
                        TextComponent {
                            append(TextComponent("\uE23C"))
                            append(TextComponent(" "))
                            append(
                                TextComponent {
                                    content("${adminChatSender.displayName}: ")
                                    color(TextColor.color(146, 169, 244))
                                }
                            )
                            append(
                                TextComponent {
                                    append(JSONComponentSerializer.json().deserialize(request.rawJsonMessage))
                                    color(TextColor.color(200, 211, 244))
                                }
                            )

                            if (!isGeyser && additionalRawJsonMessageJavaOnlyClient != null) {
                                appendNewline()
                                append(additionalRawJsonMessageJavaOnlyClient)
                            }
                        }
                    }
                }

                it.sendMessage(message)
            }

        return ProxySendAdminChatResponse.Success
    }
}