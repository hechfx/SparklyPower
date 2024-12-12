package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.serializable.SerializedWorldLocation
import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.HologramLine
import net.perfectdreams.dreamholograms.data.StoredHologram

class DreamHologramsAlignExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramToAlign = word("hologram_to_align", m.hologramNameAutocomplete)
        val referenceHologram = word("reference_hologram", m.hologramNameAutocomplete)
        val alignment = word("alignment") { context, suggests ->
            suggests.suggest("x")
            suggests.suggest("y")
            suggests.suggest("z")
        }
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val hologramToAlign = args[options.hologramToAlign]
        val referenceHologram = args[options.referenceHologram]
        val alignment = args[options.alignment]

        val hologramToAlignData = m.holograms[hologramToAlign]
        val referenceHologramData = m.holograms[referenceHologram]

        if (hologramToAlignData == null) {
            DreamHologramsCommand.sendHologramDoesNotExist(
                context,
                hologramToAlign
            )
            return
        }

        if (referenceHologramData == null) {
            DreamHologramsCommand.sendHologramDoesNotExist(
                context,
                referenceHologram
            )
            return
        }

        val originalLocation = hologramToAlignData.data.location
        val referenceLocation = hologramToAlignData.data.location

        val newLocation = originalLocation.copy(
            x = if ('x' in alignment) referenceLocation.x else originalLocation.x,
            y = if ('y' in alignment) referenceLocation.y else originalLocation.y,
            z = if ('z' in alignment) referenceLocation.z else originalLocation.z,
        )

        hologramToAlignData.data.location = newLocation
        hologramToAlignData.updateHologram()

        m.saveHologramsAsync()

        context.sendMessage {
            color(NamedTextColor.GREEN)
            content("Holograma editado!")
        }
    }
}