package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.serializable.SerializedWorldLocation
import net.perfectdreams.dreamcore.utils.LocationUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.HologramLine
import net.perfectdreams.dreamholograms.data.StoredHologram

class DreamHologramsLookAtMeExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramName = word("hologram_name", m.hologramNameAutocomplete)
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

        val yawAndPitch = LocationUtils.getYawAndPitchLookingAt(hologram.data.location.toLocation(), player.location)
        hologram.data.location = hologram.data.location.copy(
            yaw = yawAndPitch.yaw,
            pitch = yawAndPitch.pitch
        )
        hologram.updateHologram()

        m.saveHologramAsync(hologram)

        context.sendMessage {
            color(NamedTextColor.GREEN)
            content("Holograma agora está olhando para você!")
        }
    }
}