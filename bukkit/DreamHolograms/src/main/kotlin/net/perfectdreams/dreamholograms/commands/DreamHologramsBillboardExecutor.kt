package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.serializable.SerializedWorldLocation
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.displays.DisplayBlock
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.HologramLine
import net.perfectdreams.dreamholograms.data.StoredHologram
import org.bukkit.entity.Display

class DreamHologramsBillboardExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramName = word("hologram_name", m.hologramNameAutocomplete)
        val displayTextLine = word("display_text_line")
        val billboardType = word(
            "billboard_type"
        ) { context, builder ->
            Display.Billboard.entries.forEach {
                builder.suggest(it.name.lowercase())
            }
        }
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val hologramName = args[options.hologramName]
        val displayTextLine = args[options.displayTextLine]
        val billboardType = args[options.billboardType]

        val hologramData = m.holograms[hologramName]
        if (hologramData == null) {
            DreamHologramsCommand.sendHologramDoesNotExist(context, hologramName)
            return
        }

        val billboard = Display.Billboard.valueOf(billboardType.uppercase())

        val lines = if (displayTextLine == "all") {
            hologramData.data.lines.filterIsInstance<HologramLine.HologramText>()
        } else {
            listOf(hologramData.data.lines[displayTextLine.toInt() + 1] as HologramLine.HologramText)
        }

        for (line in lines) {
            line.billboard = billboard
        }
        hologramData.updateHologram()

        m.saveHologramAsync(hologramData)

        context.sendMessage {
            color(NamedTextColor.GREEN)
            content("Holograma editado!")
        }
    }
}