package net.sparklypower.sparklyvelocitycore.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandArguments
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandContext
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclaration
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.sparklyCommand
import net.sparklypower.sparklyvelocitycore.utils.commands.executors.SparklyCommandExecutor
import net.sparklypower.sparklyvelocitycore.utils.commands.options.CommandOptions
import net.sparklypower.sparklyvelocitycore.SparklyVelocityCore
import net.sparklypower.sparklyvelocitycore.SparklyVelocityPlugin
import net.sparklypower.sparklyvelocitycore.utils.commands.options.buildSuggestionsBlockFromList
import java.io.File
import kotlin.jvm.optionals.getOrNull

class VelocityPluginsCommand(val m: SparklyVelocityCore) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("velocityplugins")) {
        permission = "sparklyvelocitycore.pluginmanager"

        subcommand(listOf("load")) {
            executor = PluginLoadExecutor(m)
        }

        subcommand(listOf("unload")) {
            executor = PluginUnloadExecutor(m)
        }

        subcommand(listOf("reload")) {
            executor = PluginReloadExecutor(m)
        }
    }

    class PluginLoadExecutor(val m: SparklyVelocityCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val pluginFile = greedyString(
                "plugin_file",
                buildSuggestionsBlockFromList {
                    m.pluginManager.basePluginsDirectoryFile.listFiles()
                        .filter { it.extension == "jar" }
                        .map { it.nameWithoutExtension }
                }
            )
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val pluginName = args[options.pluginFile]
            /* val plugin = m.server.pluginManager.getPlugin(pluginName).getOrNull()
            if (plugin != null) {
                invocation.source().sendMessage("§cPlugin desconhecido!".fromLegacySectionToTextComponent())
                return
            } */

            val jarFile = File(m.pluginManager.basePluginsDirectoryFile, "$pluginName.jar")
            if (!jarFile.exists()) {
                context.sendMessage(
                    Component.text("Arquivo não existe!", NamedTextColor.RED)
                )
                return
            }
            m.pluginManager.loadPlugin(jarFile)

            context.sendMessage(
                Component.text("Plugin carregado com sucesso!", NamedTextColor.GREEN)
            )
        }
    }

    class PluginUnloadExecutor(val m: SparklyVelocityCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val pluginId = greedyString(
                "plugin_id",
                buildSuggestionsBlockFromList {
                    m.server.pluginManager.plugins.map { it.description.id }
                }
            )
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val pluginId = args[options.pluginId]
            val plugin = m.server.pluginManager.getPlugin(pluginId).getOrNull()
            if (plugin == null) {
                context.sendMessage(
                    Component.text("Plugin desconhecido!", NamedTextColor.RED)
                )
                return
            }

            m.pluginManager.unloadPlugin(plugin)
            context.sendMessage(
                Component.text("Plugin descarregado!", NamedTextColor.GREEN)
            )
        }
    }

    class PluginReloadExecutor(val m: SparklyVelocityCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val pluginId = greedyString(
                "plugin_id",
                buildSuggestionsBlockFromList {
                    m.server.pluginManager.plugins.map { it.description.id }
                }
            )
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val pluginId = args[options.pluginId]
            val plugin = m.server.pluginManager.getPlugin(pluginId).getOrNull()
            if (plugin == null) {
                context.sendMessage(
                    Component.text("Plugin desconhecido!", NamedTextColor.RED)
                )
                return
            }

            val pluginSource = plugin.description.source.getOrNull()
            if (pluginSource == null) {
                context.sendMessage(
                    Component.text("Plugin não possui um arquivo fonte!", NamedTextColor.RED)
                )
                return
            }

            m.pluginManager.unloadPlugin(plugin)
            context.sendMessage(
                Component.text("Plugin descarregado!", NamedTextColor.GREEN)
            )
            m.pluginManager.loadPlugin(pluginSource.toFile())
            context.sendMessage(
                Component.text("Plugin carregado com sucesso!", NamedTextColor.GREEN)
            )
        }
    }
}