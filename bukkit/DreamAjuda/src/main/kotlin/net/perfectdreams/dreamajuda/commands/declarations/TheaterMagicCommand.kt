package net.perfectdreams.dreamajuda.commands.declarations

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitPlayer
import com.sk89q.worldedit.entity.Player
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.world.World
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.theatermagic.AnimationAction
import net.perfectdreams.dreamajuda.theatermagic.RecordingPlaybackContext
import net.perfectdreams.dreamajuda.theatermagic.TheaterMagicStoredRecordingAnimation
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File

class TheaterMagicCommand(val m: DreamAjuda) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("theatermagic")) {
        permissions = listOf("dreamajuda.theatermagic")
        subcommand(listOf("record")) {
            permissions = listOf("dreamajuda.theatermagic")
            executor = StartRecordingExecutor(m)
        }

        subcommand(listOf("stop")) {
            permissions = listOf("dreamajuda.theatermagic")
            executor = StopRecordingExecutor(m)
        }

        subcommand(listOf("discard")) {
            permissions = listOf("dreamajuda.theatermagic")
            executor = DiscardRecordingExecutor(m)
        }

        subcommand(listOf("playback")) {
            permissions = listOf("dreamajuda.theatermagic")
            executor = PlaybackRecordingExecutor(m)
        }

        subcommand(listOf("teleporttostart")) {
            permissions = listOf("dreamajuda.theatermagic")
            executor = TeleportToStartRecordingExecutor(m)
        }

        subcommand(listOf("teleporttoend")) {
            permissions = listOf("dreamajuda.theatermagic")
            executor = TeleportToEndRecordingExecutor(m)
        }
    }

    class StartRecordingExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val fileName = greedyString("file_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val weRegion = getPlayerSelection(player)

            if (weRegion == null) {
                context.sendMessage("Você precisa criar uma região do WorldEdit indicando os blocos que devem ser registrados na gravação!")
                return
            }

            val recording = m.theaterMagicManager.startRecording(player, weRegion, args[options.fileName])
            context.sendMessage("Gravação iniciada! Encene para o público!")
        }

        fun getPlayerSelection(player: org.bukkit.entity.Player): Region? {
            // Convert Bukkit player to WorldEdit Player
            val wePlayer: BukkitPlayer = BukkitAdapter.adapt(player)

            // Get the player's session
            val session = WorldEdit.getInstance().sessionManager[wePlayer]

            // Get the player's selection region
            try {
                val selectionWorld: World = session.selectionWorld
                val region: Region = session.getSelection(selectionWorld)

                return region // Return the selected region
            } catch (e: Exception) {
                // Handle cases where the player has no selection or an invalid selection
                e.printStackTrace()
                return null
            }
        }
    }

    class StopRecordingExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val recording = m.theaterMagicManager.getActiveRecording(player)
            if (recording != null) {
                val recorded = recording.finish()
                File(m.dataFolder, "${recording.fileName}.json")
                    .writeText(
                        Json {
                            prettyPrint = true
                        }.encodeToString(recorded)
                    )

                context.sendMessage("Gravação encerrada e salva com o nome ${recording.fileName}")
            } else {
                context.sendMessage("Você não tem uma gravação ativa!")
            }
        }
    }

    class DiscardRecordingExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val recording = m.theaterMagicManager.getActiveRecording(player)
            if (recording != null) {
                val recorded = recording.finish()
                context.sendMessage("Gravação encerrada e descartada")
            } else {
                context.sendMessage("Você não tem uma gravação ativa!")
            }
        }
    }

    class PlaybackRecordingExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val fileName = greedyString("file_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val recordingFile = File(m.dataFolder, "${args[options.fileName]}.json")
            if (!recordingFile.exists()) {
                context.sendMessage("Arquivo não existe!")
                return
            }

            val recording = recordingFile.readText().let {
                Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(it)
            }

            val npc = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
                m,
                Location(Bukkit.getWorld("RevampedTutorialIsland"), 0.0, 0.0, 0.0, 0f, 0f),
                player.name,
                null
            )

            context.sendMessage("Reproduzindo Gravação...")
            val playbackContext = RecordingPlaybackContext(m, player, recording, player.world) {
                npc
            }
            m.launchMainThread {
                playbackContext.startPlayback {}.join()

                npc.remove()
                playbackContext.recordingIdToRealEntity.values.forEach {
                    it.remove()
                }

                context.sendMessage("Gravação encerrada!")
            }
        }
    }

    class TeleportToStartRecordingExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val fileName = greedyString("file_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val recordingFile = File(m.dataFolder, "${args[options.fileName]}.json")
            if (!recordingFile.exists()) {
                context.sendMessage("Arquivo não existe!")
                return
            }

            val recording = recordingFile.readText().let {
                Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(it)
            }

            val firstKeyframeWithMove = recording.keyframes.values.flatMap { it.actions }.filterIsInstance<AnimationAction.PlayerMovement>().first()

            player.teleport(firstKeyframeWithMove.location.toLocation(player.world))

            context.sendMessage("Teletransportado para o começo da gravação!")
        }
    }

    class TeleportToEndRecordingExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val fileName = greedyString("file_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val recordingFile = File(m.dataFolder, "${args[options.fileName]}.json")
            if (!recordingFile.exists()) {
                context.sendMessage("Arquivo não existe!")
                return
            }

            val recording = recordingFile.readText().let {
                Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(it)
            }

            val firstKeyframeWithMove = recording.keyframes.values.flatMap { it.actions }.filterIsInstance<AnimationAction.PlayerMovement>().last()

            player.teleport(firstKeyframeWithMove.location.toLocation(player.world))

            context.sendMessage("Teletransportado para o fim da gravação!")
        }
    }
}