package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHologram
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.HologramLine
import net.perfectdreams.dreamholograms.data.StoredHologram
import java.awt.Color

class DreamHologramsBackgroundColorExecutor(val m: DreamHolograms) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramName = word("hologram_name", m.hologramNameAutocomplete)
        val displayTextLine = word("display_text_line")

        val color = greedyString("color")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val hologramName = args[options.hologramName]
        val displayTextLine = args[options.displayTextLine]
        val color = args[options.color]

        val hologramData = m.holograms[hologramName]

        if (hologramData == null) {
            DreamHologramsCommand.sendHologramDoesNotExist(context, hologramName)
            return
        }

        when (val result = hologramData.getHologramLines<HologramLine.HologramText>(displayTextLine)) {
            is DreamHologram.GetByLineResult.Success<HologramLine.HologramText> -> {
                if (color == "reset" || color == "default") {
                    for (line in result.lines) {
                        line.backgroundColor = StoredHologram.DEFAULT_BACKGROUND_COLOR
                    }
                } else if (color.startsWith("#")) {
                    val rgb = Color.decode(color).rgb
                    for (line in result.lines) {
                        line.backgroundColor = rgb
                    }
                } else {
                    val (r, g, b, a) = color.replace(",", " ").split(" ").filter { it.isNotEmpty() }.map { it.toInt() }

                    val rgb = Color(r, g, b, a).rgb
                    for (line in result.lines) {
                        line.backgroundColor = rgb
                    }
                }

                hologramData.updateHologram()

                m.saveHologramAsync(hologramData)

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