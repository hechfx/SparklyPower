package net.perfectdreams.dreamholograms

import com.charleskorn.kaml.SequenceStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.options.SuggestsBlock
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamholograms.commands.DreamHologramsCommand
import net.perfectdreams.dreamholograms.data.StoredHologram
import net.perfectdreams.dreamholograms.listeners.DreamHologramsListener
import org.bukkit.event.Listener
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.logging.Level

class DreamHolograms : KotlinPlugin(), Listener {
	val holograms = mutableMapOf<String, DreamHologram>()
	val hologramsFolder = File(dataFolder, "holograms")
	var successfullyLoaded = false
	val saveMutex = Mutex()
	val hologramNameAutocomplete: SuggestsBlock = { context, builder ->
		this.holograms.forEach {
			builder.suggest(it.key)
		}
	}
	val yaml = Yaml(
		configuration = YamlConfiguration(
			encodeDefaults = false,
			sequenceStyle = SequenceStyle.Block
		)
	)

	override fun softEnable() {
		registerCommand(DreamHologramsCommand(this))
		registerEvents(DreamHologramsListener(this))

		loadHolograms()
	}

	override fun softDisable() {
		holograms.values.forEach {
			it.removeHologram()
		}

		holograms.clear()
	}

	fun createHologram(id: String, hologram: StoredHologram): DreamHologram {
		val dreamHologram = DreamHologram(this, id, hologram)
		dreamHologram.updateHologram()
		holograms[id] = dreamHologram
		return dreamHologram
	}

	fun reloadHolograms() {
		holograms.values.forEach {
			it.removeHologram()
		}

		holograms.clear()

		loadHolograms()
	}

	fun loadHolograms() {
		if (!hologramsFolder.exists()) {
			this.successfullyLoaded = true
			return
		}

		try {
			hologramsFolder.listFiles()
				.forEach {
					if (it.extension == "yml") {
						val hologram = yaml.decodeFromString<StoredHologram>(it.readText())

						createHologram(it.nameWithoutExtension, hologram)
					}
				}
			this.successfullyLoaded = true
		} catch (e: Exception) {
			logger.log(Level.WARNING, e) { "Something went wrong while trying to load holograms! Disabling hologram editing..." }
			this.successfullyLoaded = false
		}
	}

	/* fun saveHolograms() {
		runBlocking {
			saveMutex.withLock {
				for (hologram in holograms.values) {
					hologram.save()
				}
			}
		}
	}

	fun saveHologramsAsync() {
		launchAsyncThread {
			saveMutex.withLock {
				for (hologram in holograms.values) {
					hologram.save()
				}
			}
		}
	} */

	fun saveHologramAsync(hologram: DreamHologram) {
		launchAsyncThread {
			saveMutex.withLock {
				logger.info("Saving hologram ${hologram.id}...")

				hologramsFolder.mkdirs()

				saveFileSafely(
					yaml.encodeToString(hologram.data),
					File(hologramsFolder, hologram.id + ".yml")
				)

				logger.info("Successfully saved hologram ${hologram.id}!")
			}
		}
	}

	fun deleteHologramAsync(hologram: DreamHologram) {
		launchAsyncThread {
			saveMutex.withLock {
				logger.info("Deleting hologram ${hologram.id}...")

				hologramsFolder.mkdirs()

				File(hologramsFolder, hologram.id + ".yml").delete()

				logger.info("Successfully deleted hologram ${hologram.id}!")
			}
		}
	}

	fun saveFileSafely(content: String, targetFile: File) {
		val tempFile = File(targetFile.parent, "${targetFile.name}.tmp")

		try {
			// Write to a temporary file
			tempFile.writeText(content)

			// Atomically replace the target file with the temporary file
			Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
		} catch (e: IOException) {
			logger.log(Level.WARNING, e) { "Something went wrong while trying to save file $targetFile!" }
		} finally {
			// Clean up temp file if it exists
			if (tempFile.exists()) {
				tempFile.delete()
			}
		}
	}
}