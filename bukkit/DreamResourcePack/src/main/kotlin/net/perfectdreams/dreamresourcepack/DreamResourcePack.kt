package net.perfectdreams.dreamresourcepack

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamresourcepack.commands.DreamRPCommand
import net.perfectdreams.dreamresourcepack.commands.ResourcePackCommand
import net.perfectdreams.dreamresourcepack.listeners.MoveListener
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class DreamResourcePack : KotlinPlugin() {
	val sentToPlayer = HashSet<Player>()

	override fun softEnable() {
		super.softEnable()

		registerEvents(MoveListener(this))
		registerCommand(DreamRPCommand(this))
		registerCommand(ResourcePackCommand(this))
	}

	override fun softDisable() {
		super.softDisable()
	}
}