package net.perfectdreams.dreamajuda.commands

import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import net.kyori.adventure.util.Ticks
import net.kyori.adventure.util.TriState
import net.perfectdreams.dreamajuda.tutorials.SparklyTutorial
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.cutscenes.CutsceneEntityManager
import net.perfectdreams.dreamajuda.cutscenes.SparklyCutsceneCamera
import net.perfectdreams.dreamajuda.cutscenes.SparklyTutorialCutsceneConfig
import net.perfectdreams.dreamajuda.cutscenes.SparklyTutorialCutsceneFinalCut
import net.perfectdreams.dreamajuda.tutorials.StartTutorialSource
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffectsAwait
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamemptyworldgenerator.EmptyBiomeProvider
import net.perfectdreams.dreamemptyworldgenerator.EmptyWorldGenerator
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import java.io.File
import kotlin.time.measureTimedValue

class TutorialExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
    companion object {
        // TODO: Do everything on the same world, or do everything in different worlds?
        //  My concern is that creating a different world for each player will lag the server, because loading worlds is a bit expensive
        //  Took 13.922030ms to load the ephemeral world for jubbskkk
        //  Took 3.222899ms to unload the ephemeral world for jubbskkk
        //  It isn't that expensive, but you also need to think how to handle WHEN the world will be loaded/unloaded

        const val COPY_WORLD_FROM_TEMPLATE_WORLD = false
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        m.startBeginningCutsceneAndTutorial(player, StartTutorialSource.COMMAND)
    }
}