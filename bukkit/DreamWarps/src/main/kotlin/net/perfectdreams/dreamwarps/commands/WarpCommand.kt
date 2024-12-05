package net.perfectdreams.dreamwarps.commands

import net.perfectdreams.dreamcore.utils.TextUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.playTeleportEffects
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamwarps.DreamWarps
import net.perfectdreams.dreamwarps.events.PlayerWarpTeleportEvent

class WarpCommand(val m: DreamWarps) : SparklyCommandDeclarationWrapper {
	override fun declaration() = sparklyCommand(listOf("warp", "warps", "dwarps")) {
		executor = WarpExecutor(m)
	}

	class WarpExecutor(val m: DreamWarps) : SparklyCommandExecutor() {
		inner class Options : CommandOptions() {
			val warpName = optionalGreedyString("warp_name") { context, builder ->
				for (warp in m.warps.filter { context.sender.hasPermission("dreamwarps.warp.${it.name}") }) {
					builder.suggest(warp.name)
				}
			}
		}

		override val options = Options()

		override fun execute(context: CommandContext, args: CommandArguments) {
			val player = context.requirePlayer()
			val name = args[options.warpName]

			if (name == null) {
				m.warpsMenu.sendTo(player)
				return
			}

			val warp = m.warps.firstOrNull { it.name.equals(name, true) }

			if (warp == null) {
				context.sendMessage("${DreamWarps.PREFIX} §cNão existe nenhuma warp chamada ${name}!")
				return
			}

			if (!player.hasPermission("dreamwarps.warp.$name")) {
				player.sendMessage("${DreamWarps.PREFIX} $withoutPermission")
				return
			}

			val warpTeleportEvent = PlayerWarpTeleportEvent(player, warp.name, warp.location)
			val success = warpTeleportEvent.callEvent()
			if (!success)
				return

			val warpTarget = warpTeleportEvent.warpTarget

			player.teleportAsync(warpTarget).thenRun {
				player.playTeleportEffects()
				player.sendMessage("${DreamWarps.PREFIX} §aVocê chegou ao seu destino. §cʕ•ᴥ•ʔ")
				player.sendTitle(
					"§b${warp.fancyName}",
					"§3${TextUtils.ROUND_TO_2_DECIMAL.format(warpTarget.x)}§b, §3${TextUtils.ROUND_TO_2_DECIMAL.format(
						warpTarget.y
					)}§b, §3${TextUtils.ROUND_TO_2_DECIMAL.format(warpTarget.z)}",
					10,
					60,
					10
				)
			}
		}
	}
}