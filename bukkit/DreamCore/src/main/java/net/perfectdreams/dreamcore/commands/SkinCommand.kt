package net.perfectdreams.dreamcore.commands

import com.destroystokyo.paper.profile.ProfileProperty
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.tables.PlayerSkins
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcore.utils.skins.AshconEverythingResponse
import net.perfectdreams.dreamcore.utils.skins.SkinUtils
import net.perfectdreams.dreamcore.utils.skins.StoredDatabaseSkin
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import java.util.*

class SkinCommand(val m: DreamCore) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("skin")) {
        permissions = listOf("sparklycore.skin.set")

        executor = SetSelfSkinExecutor()

        subcommand(listOf("update")) {
            permissions = listOf("sparklycore.skin.update")

            executor = UpdateSelfSkinExecutor()
        }

        subcommand(listOf("clear")) {
            permissions = listOf("sparklycore.skin.clear")

            executor = ClearSelfSkinExecutor()
        }
    }

    inner class SetSelfSkinExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val skinName = word("skin_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val skinName = args[options.skinName]

            context.sendMessage("§eBaixando skin ${skinName}...")

            m.launchAsyncThread {
                val response = DreamUtils.http.get("https://api.ashcon.app/mojang/v2/user/$skinName")

                if (response.status == HttpStatusCode.NotFound) {
                    context.sendMessage("§cNão existe nenhum player com o nome ${skinName}!")
                    return@launchAsyncThread
                }

                val ashconResponse = JsonIgnoreUnknownKeys.decodeFromString<AshconEverythingResponse>(response.bodyAsText())

                // We will get the UUID from Ashcon's API, but we will get the skin from Mojang itself
                // We do this because the Username to UUID API is VERY VERY VERY ratelimited
                val mojangResponse = DreamUtils.http.get("https://sessionserver.mojang.com/session/minecraft/profile/${ashconResponse.uuid}?unsigned=false")

                val playerUniqueIdWithDashes: String
                val playerTextureValue: String
                val playerTextureSignature: String

                if (mojangResponse.status == HttpStatusCode.TooManyRequests) {
                    // If we get rate limited, use Ashcon's response
                    playerUniqueIdWithDashes = ashconResponse.uuid
                    playerTextureValue = ashconResponse.textures.raw.value
                    playerTextureSignature = ashconResponse.textures.raw.signature
                } else {
                    // If we aren't rate limited, use Mojang's response
                    val jsonObj = Json.parseToJsonElement(mojangResponse.bodyAsText())
                        .jsonObject

                    val mcTexturesObj = jsonObj["properties"]!!
                        .jsonArray
                        .first {
                            it.jsonObject["name"]!!.jsonPrimitive.content == "textures"
                        }
                        .jsonObject

                    playerUniqueIdWithDashes = m.skinUtils.convertNonDashedToUniqueID(jsonObj["id"]!!.jsonPrimitive.content).toString()
                    playerTextureValue = mcTexturesObj["value"]!!.jsonPrimitive.content
                    playerTextureSignature = mcTexturesObj["signature"]!!.jsonPrimitive.content
                }

                // Set the current player's skin in the database
                transaction(Databases.databaseNetwork) {
                    PlayerSkins.upsert(PlayerSkins.id) {
                        it[PlayerSkins.id] = player.uniqueId
                        it[PlayerSkins.data] = Json.encodeToString<StoredDatabaseSkin>(
                            if (ashconResponse.username == player.name) {
                                StoredDatabaseSkin.SelfMojangSkin(
                                    playerUniqueIdWithDashes,
                                    Clock.System.now(),
                                    playerTextureValue,
                                    playerTextureSignature
                                )
                            } else {
                                StoredDatabaseSkin.CustomMojangSkin(
                                    playerUniqueIdWithDashes,
                                    Clock.System.now(),
                                    playerTextureValue,
                                    playerTextureSignature
                                )
                            }
                        )
                    }
                }

                onMainThread {
                    // Update the player's profile
                    // This works, and it is WAY simpler than the whatever hacky hack SkinsRestorer is doing
                    val playerProfile = player.playerProfile
                    playerProfile.removeProperty("textures")
                    playerProfile.setProperty(
                        ProfileProperty(
                            "textures",
                            playerTextureValue,
                            playerTextureSignature,
                        )
                    )
                    player.playerProfile = playerProfile

                    context.sendMessage("§aSkin alterada!")
                }
                return@launchAsyncThread
            }
        }
    }

    inner class UpdateSelfSkinExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            m.launchAsyncThread {
                val data = transaction(Databases.databaseNetwork) {
                    val playerSkin = PlayerSkins.select { PlayerSkins.id eq player.uniqueId }
                        .limit(1)
                        .firstOrNull() ?: return@transaction null

                    Json.decodeFromString<StoredDatabaseSkin>(playerSkin[PlayerSkins.data])
                }

                if (data is StoredDatabaseSkin.NoSkin) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        append("Você não tem uma skin! Se você quiser trocar a sua skin, use ")
                        appendCommand("/skin")
                        append("!")
                    }
                    return@launchAsyncThread
                }

                if (data is StoredDatabaseSkin.MojangSkin) {
                    val response = DreamUtils.http.get("https://api.ashcon.app/mojang/v2/user/${data.mojangUUID}")

                    if (response.status == HttpStatusCode.NotFound) {
                        context.sendMessage {
                            color(NamedTextColor.RED)
                            append("Não existe uma conta da Mojang com o UUID ${data.mojangUUID}!")
                        }
                        return@launchAsyncThread
                    }

                    val ashconResponse = JsonIgnoreUnknownKeys.decodeFromString<AshconEverythingResponse>(response.bodyAsText())

                    // We will get the UUID from Ashcon's API, but we will get the skin from Mojang itself
                    // We do this because the Username to UUID API is VERY VERY VERY ratelimited
                    val mojangResponse = DreamUtils.http.get("https://sessionserver.mojang.com/session/minecraft/profile/${ashconResponse.uuid}?unsigned=false")

                    val playerUniqueIdWithDashes: String
                    val playerTextureValue: String
                    val playerTextureSignature: String

                    if (mojangResponse.status == HttpStatusCode.TooManyRequests) {
                        // If we get rate limited, use Ashcon's response
                        playerUniqueIdWithDashes = ashconResponse.uuid
                        playerTextureValue = ashconResponse.textures.raw.value
                        playerTextureSignature = ashconResponse.textures.raw.signature
                    } else {
                        // If we aren't rate limited, use Mojang's response
                        val jsonObj = Json.parseToJsonElement(mojangResponse.bodyAsText())
                            .jsonObject

                        val mcTexturesObj = jsonObj["properties"]!!
                            .jsonArray
                            .first {
                                it.jsonObject["name"]!!.jsonPrimitive.content == "textures"
                            }
                            .jsonObject

                        playerUniqueIdWithDashes = m.skinUtils.convertNonDashedToUniqueID(jsonObj["id"]!!.jsonPrimitive.content).toString()
                        playerTextureValue = mcTexturesObj["value"]!!.jsonPrimitive.content
                        playerTextureSignature = mcTexturesObj["signature"]!!.jsonPrimitive.content
                    }

                    // Set the current player's skin in the database
                    transaction(Databases.databaseNetwork) {
                        PlayerSkins.upsert(PlayerSkins.id) {
                            it[PlayerSkins.id] = player.uniqueId
                            it[PlayerSkins.data] = Json.encodeToString<StoredDatabaseSkin>(
                                when (data) {
                                    is StoredDatabaseSkin.CustomMojangSkin -> StoredDatabaseSkin.CustomMojangSkin(
                                        playerUniqueIdWithDashes,
                                        Clock.System.now(),
                                        playerTextureValue,
                                        playerTextureSignature
                                    )
                                    is StoredDatabaseSkin.SelfMojangSkin -> StoredDatabaseSkin.SelfMojangSkin(
                                        playerUniqueIdWithDashes,
                                        Clock.System.now(),
                                        playerTextureValue,
                                        playerTextureSignature
                                    )
                                }
                            )
                        }
                    }

                    onMainThread {
                        // Update the player's profile
                        // This works, and it is WAY simpler than the whatever hacky hack SkinsRestorer is doing
                        val playerProfile = player.playerProfile
                        playerProfile.removeProperty("textures")
                        playerProfile.setProperty(
                            ProfileProperty(
                                "textures",
                                playerTextureValue,
                                playerTextureSignature
                            )
                        )
                        player.playerProfile = playerProfile

                        context.sendMessage("§aSkin atualizada!")
                    }
                } else {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        append("Não é possível atualizar a sua skin!")
                    }
                }
            }
        }
    }

    inner class ClearSelfSkinExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            m.launchAsyncThread {
                transaction(Databases.databaseNetwork) {
                    PlayerSkins.upsert(PlayerSkins.id) {
                        it[PlayerSkins.id] = player.uniqueId
                        it[PlayerSkins.data] = Json.encodeToString<StoredDatabaseSkin>(StoredDatabaseSkin.NoSkin(Clock.System.now()))
                    }
                }

                onMainThread {
                    // Remove the player's current textures property
                    val playerProfile = player.playerProfile
                    playerProfile.removeProperty("textures")
                    player.playerProfile = playerProfile

                    context.sendMessage {
                        color(NamedTextColor.GREEN)
                        append("Skin removida!")
                    }
                }
            }
        }
    }
}