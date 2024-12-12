package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHolograms

class DreamHologramsRelativeMovementExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramToMove = word("hologram_to_move", m.hologramNameAutocomplete)
        val axis = word("axis") { context, suggests ->
            suggests.suggest("x")
            suggests.suggest("y")
            suggests.suggest("z")
        }
        val quantity = double("quantity")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val hologramToMove = args[options.hologramToMove]
        val axis = args[options.axis]
        val quantity = args[options.quantity]

        val hologramToMoveData = m.holograms[hologramToMove]
        if (hologramToMoveData == null) {
            DreamHologramsCommand.sendHologramDoesNotExist(context, hologramToMove)
            return
        }

        hologramToMoveData.data.location = hologramToMoveData.data.location.copy(
            x = hologramToMoveData.data.location.x + if ('x' in axis) quantity else 0.0,
            y = hologramToMoveData.data.location.y + if ('y' in axis) quantity else 0.0,
            z = hologramToMoveData.data.location.z + if ('z' in axis) quantity else 0.0
        )
        hologramToMoveData.updateHologram()

        m.saveHologramsAsync()

        context.sendMessage {
            color(NamedTextColor.GREEN)
            content("Holograma editado!")
        }
    }
}