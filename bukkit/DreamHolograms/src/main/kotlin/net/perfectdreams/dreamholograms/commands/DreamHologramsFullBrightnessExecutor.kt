package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.serializable.SerializedWorldLocation
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.displays.DisplayBlock
import net.perfectdreams.dreamholograms.DreamHologram
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.Brightness
import net.perfectdreams.dreamholograms.data.HologramLine
import net.perfectdreams.dreamholograms.data.StoredHologram
import org.bukkit.entity.Display

class DreamHologramsFullBrightnessExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramName = word("hologram_name", m.hologramNameAutocomplete)
        val displayTextLine = word("display_text_line")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val hologramName = args[options.hologramName]
        val displayTextLine = args[options.displayTextLine]

        val hologramData = m.holograms[hologramName]
        if (hologramData == null) {
            DreamHologramsCommand.sendHologramDoesNotExist(context, hologramName)
            return
        }

        when (val result = hologramData.getHologramLines<HologramLine.HologramText>(displayTextLine)) {
            is DreamHologram.GetByLineResult.Success<HologramLine.HologramText> -> {
                for (line in result.lines) {
                    line.brightness = Brightness(
                        15,
                        15
                    )
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