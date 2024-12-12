package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.serializable.SerializedWorldLocation
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.HologramLine
import net.perfectdreams.dreamholograms.data.StoredHologram

class DreamHologramsNearExecutor(val m: DreamHolograms)  : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val distance = integer("distance")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val distance = args[options.distance]
        val powedDistance = distance * distance

        val hologramsNearMe = m.holograms.filter {
            if (it.value.data.location.worldName != player.world.name)
                false
            else {
                val bukkitLocation = it.value.data.location.toLocation()
                powedDistance > bukkitLocation.distanceSquared(player.location)
            }
        }

        if (hologramsNearMe.isEmpty()) {
            context.sendMessage {
                color(NamedTextColor.RED)
                content("Não tem nenhum holograma perto de você!")
            }
        } else {
            context.sendMessage {
                color(NamedTextColor.YELLOW)
                appendTextComponent {
                    content("Hologramas perto de você:")
                    appendNewline()
                }

                for (hologram in hologramsNearMe) {
                    appendTextComponent {
                        color(NamedTextColor.AQUA)
                        append(hologram.key)
                    }
                    appendNewline()
                }
            }
        }
    }
}