package net.perfectdreams.dreamholograms

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.perfectdreams.dreamholograms.data.HologramLine
import net.perfectdreams.dreamholograms.data.StoredHologram
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f
import java.util.*

class DreamHologram(
    val m: DreamHolograms,
    val data: StoredHologram
) {
    private val hologramEntitiesUniqueIds = mutableListOf<UUID>()

    inline fun <reified T : HologramLine> getHologramLines(displayTextLine: String): GetByLineResult<T> {
        if (displayTextLine == "all")
            return GetByLineResult.Success(this.data.lines.filterIsInstance<T>())
        else {
            val displayLinePosition = displayTextLine.toIntOrNull() ?: return GetByLineResult.InvalidLine()

            val displayLineIndex = displayLinePosition - 1

            val displayLine = this.data.lines.getOrNull(displayLineIndex) ?: return GetByLineResult.LineDoesNotExist()

            if (displayLine !is T)
                return GetByLineResult.InvalidDisplayType()

            return GetByLineResult.Success(listOf(displayLine))
        }
    }

    sealed class GetByLineResult<T> {
        data class Success<T>(val lines: List<T>) : GetByLineResult<T>()
        class InvalidLine<T> : GetByLineResult<T>()
        class LineDoesNotExist<T> : GetByLineResult<T>()
        class InvalidDisplayType<T> : GetByLineResult<T>()
    }

    fun isInChunk(chunk: Chunk): Boolean {
        if (data.location.worldName != chunk.world.name)
            return false

        val chunkKey = Chunk.getChunkKey(data.location.x.toInt() shr 4, data.location.z.toInt() shr 4)
        return chunkKey == chunk.chunkKey
    }

    fun spawnHologram() {
        updateHologram()
    }

    fun updateHologram() {
        // We always remove the current hologram entities when updating the hologram
        removeHologram()

        // Don't attempt to spawn hologram if the world is not loaded yet
        val world = Bukkit.getWorld(data.location.worldName)
        if (world == null) {
            m.logger.info("Not spawning hologram ${data.id} because the world does not exist!")
            return
        }

        val location = data.location.toLocation(world)

        if (!location.isChunkLoaded) {
            m.logger.info("Not spawning hologram ${data.id} because the chunk that the hologram is within is not loaded!")
            return
        }

        m.logger.info("Spawning hologram ${data.id}...")

        val chunk = world.getChunkAt(location)
        chunk.isLoaded
        var yOffset = 0.0

        for (line in data.lines) {
            when (line) {
                is HologramLine.HologramItem -> {
                    // This is where the item should be "spawned" (somewhat, because this is where the hologram is spawned and where the item is equipped
                    yOffset -= 0.5

                    // And this is where the next display should spawn
                    yOffset -= 0.3

                    val text = location.world.spawn(
                        location.clone().add(0.0, yOffset, 0.0),
                        TextDisplay::class.java
                    ) {
                        it.isPersistent = false
                    }

                    val itemStack = location.world.dropItem(
                        location.clone().add(0.0, yOffset, 0.0),
                        ItemStack.deserializeBytes(Base64.getDecoder().decode(line.serializedItem))
                    ) {
                        it.isPersistent = false
                        it.isUnlimitedLifetime = true
                        it.setCanMobPickup(false)
                        it.setCanPlayerPickup(false)
                        it.setWillAge(false)
                        it.setGravity(false)
                    }

                    text.addPassenger(itemStack)

                    hologramEntitiesUniqueIds.add(itemStack.uniqueId)
                    hologramEntitiesUniqueIds.add(text.uniqueId)
                }
                is HologramLine.HologramText -> {
                    val component = MiniMessage.miniMessage().deserialize(line.text)

                    val transformationScale = Vector3f()
                    line.transformation.getScale(transformationScale)
                    val content = PlainTextComponentSerializer.plainText().serialize(component)

                    // Very hacky!!!
                    yOffset -= (0.3 * transformationScale.y) * (content.count { it == '\n' } + 1)

                    val text = location.world.spawn(
                        location.clone().add(0.0, yOffset, 0.0),
                        TextDisplay::class.java
                    ) {
                        it.isPersistent = false
                        it.billboard = line.billboard
                        it.backgroundColor = line.backgroundColor.let { Color.fromARGB(it) }
                        it.isShadowed = line.textShadow
                        it.setTransformationMatrix(line.transformation)
                        it.lineWidth = line.lineWidth
                        it.isSeeThrough = line.seeThrough
                        it.textOpacity = line.textOpacity
                        it.alignment = line.alignment
                        it.brightness = line.brightness?.let {
                            Display.Brightness(it.blockLight, it.skyLight)
                        }

                        it.text(component)
                    }

                    hologramEntitiesUniqueIds.add(text.uniqueId)
                }
            }
        }
    }

    fun removeHologram() {
        for (uniqueId in hologramEntitiesUniqueIds) {
            val entity = Bukkit.getEntity(uniqueId)
            entity?.remove()
        }
        hologramEntitiesUniqueIds.clear()
    }
}