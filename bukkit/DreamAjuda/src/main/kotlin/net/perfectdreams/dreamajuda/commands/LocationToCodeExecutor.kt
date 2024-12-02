package net.perfectdreams.dreamajuda.commands

import kotlinx.datetime.Clock
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.kyori.adventure.util.Ticks
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.npc.SparklyNPCManager
import net.perfectdreams.dreamcore.utils.npc.user.UserCreatedNPCData
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreamcore.utils.skins.SkinUtils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class LocationToCodeExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        context.sendMessage {
            content("[Clique para copiar a localização atual para o clipboard em formato Bukkit API!]")

            // Location(revampedTutorialIslandWorld, 6.5, 105.9, 16.5, 90.0f, 0.0f)
            clickEvent(
                ClickEvent.copyToClipboard(
                    buildString {
                        append("Location(")
                        append("Bukkit.getWorld(\"${player.world.name}\")")
                        append(", ")
                        append(player.location.x)
                        append(", ")
                        append(player.location.y)
                        append(", ")
                        append(player.location.z)
                        append(", ")
                        append(player.location.yaw)
                        append("f, ")
                        append(player.location.pitch)
                        append("f")
                        append(")")
                    }
                )
            )
        }

        context.sendMessage {
            content("[Clique para copiar a localização atual para o clipboard em formato Bukkit API! (para display entity)]")

            // Location(revampedTutorialIslandWorld, 6.5, 105.9, 16.5, 90.0f, 0.0f)
            clickEvent(
                ClickEvent.copyToClipboard(
                    buildString {
                        append("Location(")
                        append("Bukkit.getWorld(\"${player.world.name}\")")
                        append(", ")
                        append(player.location.x)
                        append(", ")
                        append(player.location.y + 1.8)
                        append(", ")
                        append(player.location.z)
                        append(", ")
                        append(player.location.yaw)
                        append("f, ")
                        append(player.location.pitch)
                        append("f")
                        append(")")
                    }
                )
            )
        }

        context.sendMessage {
            content("[Clique para copiar a localização atual para o clipboard em formato YAML!]")

            // Location(revampedTutorialIslandWorld, 6.5, 105.9, 16.5, 90.0f, 0.0f)
            clickEvent(
                ClickEvent.copyToClipboard(
                    buildString {
                        appendLine("x: ${player.location.x}")
                        appendLine("y: ${player.location.y}")
                        appendLine("z: ${player.location.z}")
                        appendLine("yaw: ${player.location.yaw}")
                        appendLine("pitch: ${player.location.pitch}")
                    }
                )
            )
        }

        context.sendMessage {
            content("[Clique para copiar a localização atual para o clipboard em formato YAML! (para display entity)]")

            // Location(revampedTutorialIslandWorld, 6.5, 105.9, 16.5, 90.0f, 0.0f)
            clickEvent(
                ClickEvent.copyToClipboard(
                    buildString {
                        appendLine("x: ${player.location.x}")
                        appendLine("y: ${player.location.y + 1.8}")
                        appendLine("z: ${player.location.z}")
                        appendLine("yaw: ${player.location.yaw}")
                        appendLine("pitch: ${player.location.pitch}")
                    }
                )
            )
        }
    }

    suspend fun sendTitleAndWait(
        player: Player,
        title: Title
    ) {
        player.showTitle(
            title
        )

        val timesPart = title.part(TitlePart.TIMES)
        val fadeIn = timesPart.fadeIn().toMillis() / Ticks.SINGLE_TICK_DURATION_MS
        val stay = timesPart.stay().toMillis() / Ticks.SINGLE_TICK_DURATION_MS
        val fadeOut = timesPart.fadeOut().toMillis() / Ticks.SINGLE_TICK_DURATION_MS
        delayTicks(fadeIn + stay + fadeOut)
    }
}