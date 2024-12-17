package net.perfectdreams.dreambedrockintegrations

import com.comphenix.protocol.ProtocolLibrary
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreambedrockintegrations.packetlisteners.BedrockPacketListener
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.network.socket.SocketReceivedEvent
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.sparklypower.rpc.proxy.ProxyGeyserStatusRequest
import net.sparklypower.rpc.proxy.ProxyGeyserStatusResponse
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class DreamBedrockIntegrations : KotlinPlugin(), Listener {
	private val ncpHook = Hook(this)
	val geyserUsers = Collections.synchronizedSet(
		mutableSetOf<UUID>()
	)
	val inventoryTitleTransformers = mutableListOf<InventoryTitleTransformer>()

	override fun softEnable() {
		super.softEnable()

		// Disabled because we also want to block Bedrock cheats
		// NCPHookManager.addHook(CheckType.ALL, ncpHook)

		val protocolManager = ProtocolLibrary.getProtocolManager()
		protocolManager.addPacketListener(BedrockPacketListener(this))

		registerEvents(this)
	}

	override fun softDisable() {
		super.softDisable()

		// NCPHookManager.removeHook(ncpHook)
	}

	fun registerInventoryTitleTransformer(
		plugin: Plugin,
		matchInventory: (Component) -> (Boolean),
		newInventoryName: (Component) -> (Component)
	) {
		inventoryTitleTransformers.add(
			InventoryTitleTransformer(
				plugin,
				matchInventory,
				newInventoryName
			)
		)
	}

	@EventHandler
	fun onDisable(event: PluginDisableEvent) {
		inventoryTitleTransformers.removeIf { it.plugin == event.plugin }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onAsyncPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent) {
		// Check if the connection is Geyser, attempt to bootstrap the connection
		runBlocking {
			try {
				withTimeout(5_000) {
					val response = DreamCore.INSTANCE.rpc.proxy.makeRPCRequest<ProxyGeyserStatusResponse>(
						ProxyGeyserStatusRequest(event.uniqueId)
					)
					logger.info("Geyser Connection Check for ${event.name} (${event.uniqueId}) is ${DefaultFontInfo.r}")

					when (response) {
						is ProxyGeyserStatusResponse.Success -> {
							if (response.isGeyser)
								geyserUsers.add(event.uniqueId)
							else
								geyserUsers.remove(event.uniqueId)
						}
						ProxyGeyserStatusResponse.UnknownPlayer -> {
							event.disallow(
								AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
								textComponent {
									color(NamedTextColor.RED)
									content("Algo deu errado ao tentar realizar o seu login!")
								}
							)
						}
					}
				}
			} catch (e: Exception) {
				logger.log(Level.WARNING, e) { "Something went wrong while trying to attempt connection bootstrap!" }
				event.disallow(
					AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
					textComponent {
						color(NamedTextColor.RED)
						content("Algo deu errado ao tentar realizar o seu login!")
					}
				)
			}
		}
	}

	@EventHandler
	fun onSocketListener(event: SocketReceivedEvent) {
		val obj = event.json.obj
		val type = obj["type"].nullString

		val removeFromList = type == "removeFromGeyserPlayerList"
		val addToList = type == "addToGeyserPlayerList"
		val uniqueId = obj["uniqueId"].nullString ?: return

		val uuid = UUID.fromString(uniqueId)
		if (addToList) {
			geyserUsers.add(uuid)
		} else if (removeFromList) {
			geyserUsers.remove(uuid)
		}
	}

	data class InventoryTitleTransformer(
		val plugin: Plugin,
		val matchInventory: (Component) -> (Boolean),
		val newInventoryName: (Component) -> (Component)
	)
}