package net.perfectdreams.dreamajuda.commands

import net.minecraft.world.level.*
import net.perfectdreams.dreamajuda.*
import net.perfectdreams.dreamajuda.commands.Tutorial3Executor.CutsceneSection
import net.perfectdreams.dreamajuda.tutorials.SparklyTutorial
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import org.bukkit.*
import java.util.*
import kotlin.reflect.KClass

class TutorialTesterExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val section = word("tutorial")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val section = args[options.section]

        val activeTutorial = m.activeTutorials[player]
        if (activeTutorial != null) {
            m.endTutorial(player)
            context.sendMessage("Tutorial encerrado")
            return
        }

        val klazz = Class.forName("net.perfectdreams.dreamajuda.tutorials.SparklyTutorial\$$section").kotlin as KClass<out SparklyTutorial>

        m.startTutorial(player, klazz)

        context.sendMessage("Tutorial $klazz ativado")
    }
}