package net.perfectdreams.dreamajuda.commands.declarations

import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.commands.*
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class Tutorial3Command(val m: DreamAjuda) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("tutorial3")) {
        permission = "dreamajuda.cutscenetester"
        executor = Tutorial3Executor(m)
    }
}