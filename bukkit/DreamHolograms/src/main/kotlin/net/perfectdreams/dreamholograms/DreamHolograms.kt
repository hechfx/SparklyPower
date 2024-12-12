package net.perfectdreams.dreamholograms

import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.perfectdreams.dreamcore.DreamCore
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
	val hologramsFile = File(dataFolder, "holograms.yml")
	var successfullyLoaded = false
	val saveMutex = Mutex()
	val hologramNameAutocomplete: SuggestsBlock = { context, builder ->
		this.holograms.forEach {
			builder.suggest(it.value.data.id)
		}
	}

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

	fun createHologram(hologram: StoredHologram): DreamHologram {
		val dreamHologram = DreamHologram(this, hologram)
		dreamHologram.updateHologram()
		holograms[hologram.id] = dreamHologram
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
		if (!hologramsFile.exists()) {
			this.successfullyLoaded = true
			return
		}

		try {
			val holograms = Yaml.default.decodeFromString<List<StoredHologram>>(hologramsFile.readText())
			holograms.forEach {
				createHologram(it)
			}
			this.successfullyLoaded = true
		} catch (e: Exception) {
			logger.log(Level.WARNING, e) { "Something went wrong while trying to load holograms! Disabling hologram editing..." }
			this.successfullyLoaded = false
		}
	}

	fun saveHolograms() {
		logger.info("Saving holograms...")
		dataFolder.mkdirs()
		saveFileSafely(
			Yaml.default.encodeToString(this.holograms.values.map { it.data }),
			hologramsFile
		)
		logger.info("Holograms saved successfully!")
	}

	fun saveHologramsAsync() {
		launchAsyncThread {
			saveMutex.withLock {
				saveHolograms()
			}
		}
	}

	private fun saveFileSafely(content: String, targetFile: File) {
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