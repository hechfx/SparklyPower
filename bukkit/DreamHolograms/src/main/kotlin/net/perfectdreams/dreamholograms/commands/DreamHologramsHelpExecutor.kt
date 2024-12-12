package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHolograms

class DreamHologramsHelpExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage {
            appendTextComponent {
                color(NamedTextColor.DARK_PURPLE)
                decorate(TextDecoration.BOLD)
                content("DreamHolograms")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.YELLOW)
                content("Um plugin de hologramas show de bola!")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms create")
                append(" - Cria um holograma")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms movehere")
                append(" - Move um holograma até você")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms lookatme")
                append(" - Faz o holograma olhar para você (precisa deixar as linhas como billboard fixed ou similar!)")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms delete")
                append(" - Deleta um holograma")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms relmove")
                append(" - Move um holograma relativamente, podendo mover ele em xyz com mais precisão")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms near")
                append(" - Mostra o ID de hologramas que estão perto de você")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms align")
                append(" - Alinha a localização de um holograma usando outro holograma como referência")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms center")
                append(" - Centraliza o holograma")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms teleport")
                append(" - Te teletransporta para o holograma")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms save")
                append(" - Salva os hologramas do servidor")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms reload")
                append(" - Recarrega os hologramas")
            }

            appendNewline()

            appendTextComponent {
                color(NamedTextColor.GRAY)
                appendCommand("/dreamholograms lines")
                append(" - Gerencia linhas de hologramas")
            }
        }
    }
}