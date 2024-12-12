package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.serializable.SerializedWorldLocation
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

class DreamHologramsCreateTextExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val hologramName = word("hologram_name")

        val text = optionalGreedyString("hologram_text")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val hologramName = args[options.hologramName]
        val text = args[options.text] ?: "Olá, eu sou o holograma <aqua>$hologramName</aqua>! :3"

        if (m.holograms.contains(hologramName)) {
            context.sendMessage {
                color(NamedTextColor.RED)
                content("Já existe um holograma com este nome!")
            }
            return
        }

        val hologram = m.createHologram(
            hologramName,
            StoredHologram(
                SerializedWorldLocation(
                    player.location.world.name,
                    player.location.x,
                    player.location.y,
                    player.location.z,
                    player.location.yaw,
                    player.location.pitch
                ),
                mutableListOf(
                    HologramLine.HologramText(
                        text,
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
            )
        )

        m.saveHologramAsync(hologram)

        context.sendMessage {
            color(NamedTextColor.GREEN)
            content("Holograma criado!")
        }
    }
}