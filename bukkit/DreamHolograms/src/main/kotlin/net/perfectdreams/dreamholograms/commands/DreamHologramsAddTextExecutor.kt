package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.HologramLine
import net.perfectdreams.dreamholograms.data.StoredHologram
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.joml.Matrix4f

class DreamHologramsAddTextExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramName =  word("hologram_name", m.hologramNameAutocomplete)

        val text = greedyString("hologram_text")
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

        hologram.data.lines
            .add(
                HologramLine.HologramText(
                    args[options.text],
                    Display.Billboard.CENTER,
                    StoredHologram.DEFAULT_BACKGROUND_COLOR,
                    false,
                    Int.MAX_VALUE,
                    false,
                    -1,
                    TextDisplay.TextAlignment.CENTER,
                    Matrix4f(),
                    null
                )
            )

        hologram.updateHologram()

        m.saveHologramsAsync()

        context.sendMessage {
            color(NamedTextColor.GREEN)
            content("Linha adicionada!")
        }
    }
}