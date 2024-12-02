package net.perfectdreams.dreamemptyworldgenerator

import org.bukkit.plugin.java.JavaPlugin

class DreamEmptyWorldGenerator : JavaPlugin() {
	private val generator = EmptyWorldGenerator()
	private val biomeProvider = EmptyBiomeProvider()

	override fun getDefaultBiomeProvider(worldName: String, id: String?) = biomeProvider

	override fun getDefaultWorldGenerator(worldName: String, id: String?) = generator
}