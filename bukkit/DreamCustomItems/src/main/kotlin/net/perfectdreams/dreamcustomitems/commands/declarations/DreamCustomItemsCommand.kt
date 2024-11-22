package net.perfectdreams.dreamcustomitems.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.commands.CustomItemsGiveExecutor
import net.perfectdreams.dreamcustomitems.commands.CustomItemsGiveRegistryExecutor
import net.perfectdreams.dreamcustomitems.commands.CustomItemsMetaExecutor
import net.perfectdreams.dreamcustomitems.commands.CustomItemsReloadRegistryExecutor

class DreamCustomItemsCommand(val m: DreamCustomItems) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamcustomitems")) {
        permissions = listOf("dreamcustomitems.setup")

        subcommand(listOf("give")) {
            executor = CustomItemsGiveExecutor()
            permissions = listOf("dreamcustomitems.setup")
        }

        subcommand(listOf("giveregistry")) {
            executor = CustomItemsGiveRegistryExecutor()
            permissions = listOf("dreamcustomitems.setup")
        }

        subcommand(listOf("reloadregistry")) {
            executor = CustomItemsReloadRegistryExecutor(m)
            permissions = listOf("dreamcustomitems.setup")
        }

        subcommand(listOf("meta")) {
            executor = CustomItemsMetaExecutor()
            permissions = listOf("dreamcustomitems.setup")
        }
    }
}