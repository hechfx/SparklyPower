package net.perfectdreams.dreamemptyworldgenerator

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import java.util.*

class EmptyWorldGenerator : ChunkGenerator() {
	override fun getFixedSpawnLocation(world: World, random: Random) = Location(world, 0.0, 128.0, 0.0)
}