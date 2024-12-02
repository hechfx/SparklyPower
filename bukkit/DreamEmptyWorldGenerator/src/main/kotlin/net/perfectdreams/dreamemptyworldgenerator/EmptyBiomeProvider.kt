package net.perfectdreams.dreamemptyworldgenerator

import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo

class EmptyBiomeProvider : BiomeProvider() {
    override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome {
        return Biome.PLAINS
    }

    override fun getBiomes(worldInfo: WorldInfo): MutableList<Biome> {
        return mutableListOf(Biome.PLAINS)
    }
}