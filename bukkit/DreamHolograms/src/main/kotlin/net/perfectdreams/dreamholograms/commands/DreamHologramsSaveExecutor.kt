package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHolograms

class DreamHologramsSaveExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        for (hologram in m.holograms.values) {
            m.saveHologramAsync(hologram)
        }

        context.sendMessage {
            color(NamedTextColor.GREEN)
            content("Hologramas salvos!")
        }
    }
}