package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHologram
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.HologramLine

class DreamHologramsSetTextContentExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramName = word("hologram_name", m.hologramNameAutocomplete)
        val displayTextLine = word("display_text_line")
        val text = greedyString("text")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val hologramName = args[options.hologramName]
        val displayTextLine = args[options.displayTextLine]
        val text = args[options.text]
        val hologram = m.holograms[hologramName]
        if (hologram == null) {
            DreamHologramsCommand.sendHologramDoesNotExist(context, hologramName)
            return
        }

        when (val result = hologram.getHologramLines<HologramLine.HologramText>(displayTextLine)) {
            is DreamHologram.GetByLineResult.Success<HologramLine.HologramText> -> {
                for (line in result.lines) {
                    line.text = text
                }

                hologram.updateHologram()

                m.saveHologramAsync(hologram)

                context.sendMessage {
                    color(NamedTextColor.GREEN)
                    content("Holograma editado!")
                }
            }
            is DreamHologram.GetByLineResult.InvalidDisplayType -> DreamHologramsCommand.sendInvalidDisplayType<HologramLine.HologramText>(context)
            is DreamHologram.GetByLineResult.InvalidLine -> DreamHologramsCommand.sendInvalidLine(context)
            is DreamHologram.GetByLineResult.LineDoesNotExist -> DreamHologramsCommand.sendLineDoesNotExist(context)
        }
    }
}