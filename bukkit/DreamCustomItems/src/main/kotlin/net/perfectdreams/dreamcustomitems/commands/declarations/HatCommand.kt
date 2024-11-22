package net.perfectdreams.dreamcustomitems.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.commands.*

class HatCommand(val m: DreamCustomItems) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("hat", "capacete", "chapéu")) {
        executor = HatExecutor()
    }
}