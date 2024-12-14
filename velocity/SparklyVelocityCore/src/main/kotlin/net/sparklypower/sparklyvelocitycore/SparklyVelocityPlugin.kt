package net.sparklypower.sparklyvelocitycore

import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.Command
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.proxy.VelocityServer
import net.sparklypower.sparklyvelocitycore.utils.commands.VelocityBrigadierCommandConverter
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclaration
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclarationWrapper

abstract class SparklyVelocityPlugin {
    internal val registeredCommands = mutableListOf<CommandMeta>()

    open fun onEnable() {}
    open fun onDisable() {}

    fun registerCommand(server: ProxyServer, declarationWrapper: SparklyCommandDeclarationWrapper) {
        val declaration = declarationWrapper.declaration()

        val commandWrappers = declaration.labels.map {
            VelocityBrigadierCommandConverter(
                it,
                declaration
            )
        }

        val velocityBrigadierCommands = commandWrappers.map { BrigadierCommand(it.convertRootDeclarationToBrigadier()) }

        for (velocityBrigadierCommand in velocityBrigadierCommands) {
            val meta = server.commandManager
                .metaBuilder(velocityBrigadierCommand)
                .plugin(this)
                .build()
            registerCommand(server, meta, velocityBrigadierCommand)
        }
    }

    fun registerCommand(server: ProxyServer, meta: CommandMeta, command: Command) {
        server.commandManager.register(meta, command)
        registeredCommands.add(meta)
    }
}