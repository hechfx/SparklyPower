package net.perfectdreams.dreamajuda.commands.declarations

import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.commands.AjudaExecutor
import net.perfectdreams.dreamajuda.commands.LocationToCodeExecutor
import net.perfectdreams.dreamajuda.commands.TransformRulesSignExecutor
import net.perfectdreams.dreamajuda.commands.TutorialExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class LocationToCodeCommand(val m: DreamAjuda) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("locationtocode")) {
        executor = LocationToCodeExecutor(m)
    }
}