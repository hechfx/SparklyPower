package net.perfectdreams.dreamajuda.commands.declarations

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.commands.TransformRulesSignExecutor
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class DreamAjudaCommand(val m: DreamAjuda) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamajuda")) {
        subcommand(listOf("regras")) {
            permissions = listOf("dreamajuda.setup")

            subcommand(listOf("transform")) {
                permissions = listOf("dreamajuda.setup")
                executor = TransformRulesSignExecutor(m)
            }
        }

        subcommand(listOf("reload")) {
            permissions = listOf("dreamajuda.setup")

            executor = ReloadExecutor(m)
        }

        subcommand(listOf("playerstutorial")) {
            permissions = listOf("dreamajuda.setup")

            executor = PlayersTutorialxecutor(m)
        }
    }

    class ReloadExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            m.reload()
            context.sendMessage("Configuração recarregada!")
        }
    }

    class PlayersTutorialxecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            context.sendMessage {
                content("Players no Tutorial (${m.activeTutorials.size}):")
                color(NamedTextColor.AQUA)
            }

            if (m.activeTutorials.isNotEmpty()) {
                for (entry in m.activeTutorials) {
                    context.sendMessage {
                        appendTextComponent {
                            content(entry.value.player.name)
                            color(NamedTextColor.AQUA)
                        }

                        appendTextComponent {
                            content(": ${entry.value.activeTutorial::class.simpleName}")
                            color(NamedTextColor.GRAY)
                        }
                    }
                }
            } else {
                context.sendMessage {
                    content("*ninguém*")
                    color(NamedTextColor.GRAY)
                }
            }
        }
    }
}