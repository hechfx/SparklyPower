package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.HologramLine
import net.perfectdreams.dreamholograms.data.StoredHologram

class DreamHologramsRemoveLineExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramName = word("hologram_name", m.hologramNameAutocomplete)

        val line = integer("line")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val hologramName = args[options.hologramName]
        val hologram = m.holograms[hologramName]
        if (hologram == null) {
            DreamHologramsCommand.sendHologramDoesNotExist(context, hologramName)
            return
        }

        // TODO: Check if there are lines
        hologram.data.lines.removeAt(args[options.line] - 1)

        hologram.updateHologram()

        m.saveHologramAsync(hologram)

        context.sendMessage {
            color(NamedTextColor.GREEN)
            content("Linha removida!")
        }
    }
}