package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHolograms

class DreamHologramsCenterExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramName = word("hologram_name", m.hologramNameAutocomplete)
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val hologramToMove = args[options.hologramName]

        val hologramToMoveData = m.holograms[hologramToMove]
        if (hologramToMoveData == null) {
            DreamHologramsCommand.sendHologramDoesNotExist(context, hologramToMove)
            return
        }

        hologramToMoveData.data.location = hologramToMoveData.data.location.copy(
            x = hologramToMoveData.data.location.x.toInt() + 0.5,
            y = hologramToMoveData.data.location.y.toInt() + 0.5,
            z = hologramToMoveData.data.location.z.toInt() + 0.5
        )
        hologramToMoveData.updateHologram()

        m.saveHologramsAsync()

        context.sendMessage {
            color(NamedTextColor.GREEN)
            content("Holograma editado!")
        }
    }
}