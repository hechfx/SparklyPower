package net.perfectdreams.dreamajuda.commands.declarations

import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.commands.*
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class CutsceneCommand(val m: DreamAjuda) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("cutscene")) {
        permission = "dreamajuda.cutsceneplayback"
        executor = TutorialExecutor(m)
    }
}