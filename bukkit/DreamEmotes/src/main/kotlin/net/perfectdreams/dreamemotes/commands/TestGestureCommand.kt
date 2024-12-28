package net.perfectdreams.dreamemotes.commands

import kotlinx.serialization.json.Json
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamemotes.DreamEmotes
import net.perfectdreams.dreamemotes.blockbench.BlockbenchModel
import net.perfectdreams.dreamemotes.gestures.SparklyGestures
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.io.File

class TestGestureCommand(val m: DreamEmotes) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("testgesture")) {
        permission = "dreamemotes.setup"
        executor = EmoteExecutor(m)
    }

    class EmoteExecutor(val m: DreamEmotes) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val file = word("file")
            val animation = word("animation")
            val type = word("type")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val requestedLocation = player.location

            val blockbenchModel = Json {
                ignoreUnknownKeys = true
            }.decodeFromString<BlockbenchModel>(File(m.dataFolder, args[options.file] + ".bbmodel").readText())

            val animation = args[options.animation]
            val type = args[options.type]
            val bbAnimation = blockbenchModel.animations.first { it.name == animation }

            m.launchAsyncThread {
                val gestureSkinHeads = m.gesturesManager.getOrCreatePlayerGesturePlaybackSkins(player)

                onMainThread {
                    val currentPlayerLocation = player.location

                    if (requestedLocation.world == currentPlayerLocation.world && 2 >= currentPlayerLocation.distanceSquared(requestedLocation)) {
                        // Cancel current gesture just for us to get the CORRECT exit location of the player
                        m.gesturesManager.stopGesturePlayback(player)

                        m.gesturesManager.createGesturePlayback(
                            player,
                            currentPlayerLocation,
                            gestureSkinHeads,
                            blockbenchModel,
                            when (type) {
                                "play_and_loop" -> SparklyGestures.SparklyGesture(animation, listOf(SparklyGestures.GestureAction.PlayAndLoop(bbAnimation, mapOf(), { _, _ -> })))
                                "play_and_hold" -> SparklyGestures.SparklyGesture(animation, listOf(SparklyGestures.GestureAction.PlayAndHold(bbAnimation, mapOf(), { _, _ -> })))
                                "play_once" -> SparklyGestures.SparklyGesture(animation, listOf(SparklyGestures.GestureAction.Play(bbAnimation, mapOf(), { _, _ -> })))
                                else -> error("Unknown gesture action type!")
                            }
                        )
                    } else {
                        player.sendMessage(
                            textComponent {
                                color(NamedTextColor.RED)
                                content("Você se moveu enquanto o gesto estava sendo carregado! Se você quiser usar o gesto, use o comando novamente.")
                            }
                        )
                    }
                }
            }
        }
    }
}