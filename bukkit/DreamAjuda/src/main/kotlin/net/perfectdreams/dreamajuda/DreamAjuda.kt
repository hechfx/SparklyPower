package net.perfectdreams.dreamajuda

import com.charleskorn.kaml.Yaml
import com.google.common.collect.Sets
import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamajuda.commands.declarations.*
import net.perfectdreams.dreamajuda.configs.RevampedTutorialConfig
import net.perfectdreams.dreamajuda.cutscenes.*
import net.perfectdreams.dreamajuda.theatermagic.TheaterMagicListener
import net.perfectdreams.dreamajuda.theatermagic.TheaterMagicManager
import net.perfectdreams.dreamajuda.tutorials.PlayerTutorial
import net.perfectdreams.dreamajuda.tutorials.SparklyTutorial
import net.perfectdreams.dreamajuda.tutorials.RevampedTutorialListener
import net.perfectdreams.dreamajuda.tutorials.StartTutorialSource
import net.perfectdreams.dreambedrockintegrations.utils.isBedrockClient
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.*
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import org.bukkit.*
import org.bukkit.block.Sign
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import java.io.File
import java.util.logging.Level
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class DreamAjuda : KotlinPlugin(), Listener {
	companion object {
		val RULES_VERSION = SparklyNamespacedKey("rules_version", PersistentDataType.INTEGER)
		val IS_RULES_SIGN = SparklyNamespacedBooleanKey("is_rules_sign")
	}

	val theaterMagicManager = TheaterMagicManager(this)
	val activeTutorials = mutableMapOf<Player, PlayerTutorial>()
	lateinit var tutorialConfig: RevampedTutorialConfig
	lateinit var tutorialCutsceneConfig: SparklyTutorialCutsceneConfig
	val cutsceneJobs = mutableMapOf<Player, Job>()
	lateinit var cutsceneCache: CutsceneCache
	val metroSigns = mutableListOf<TextDisplay>()
	private val isLoadingCutscene = Sets.newHashSet<Player>()

	override fun softEnable() {
		super.softEnable()

		reload()

		registerEvents(this)
		registerEvents(TheaterMagicListener(theaterMagicManager))
		registerEvents(BypassWGListener())
		registerEvents(RevampedTutorialListener(this))
		registerCommand(AjudaCommand(this))
		registerCommand(DreamAjudaCommand(this))
		// registerCommand(TutorialCommand(this))
		// registerCommand(Tutorial2Command(this))
		registerCommand(Tutorial3Command(this))
		registerCommand(LocationToCodeCommand(this))
		registerCommand(TheaterMagicCommand(this))
		registerCommand(TutorialCommand(this))
		registerCommand(TutorialTesterCommand(this))
		registerCommand(SkipTutorialCommand(this))
	}

	override fun softDisable() {
		super.softDisable()

		// Remove all subway signs
		this.metroSigns.forEach { it.remove() }
		this.metroSigns.clear()

		// End the tutorial of everyone that is in the tutorial during shutdown
		activeTutorials.toMap().forEach { (t, u) ->
			endTutorial(t)
			t.teleportToServerSpawnWithEffects()
		}
	}

	fun reload() {
		this.tutorialConfig = Yaml.default.decodeFromString(File(this.dataFolder, "tutorial.yml").readText())
		this.tutorialCutsceneConfig = Yaml.default.decodeFromString<SparklyTutorialCutsceneConfig>(File(this@DreamAjuda.dataFolder, "cutscene_tutorial.yml").readText())
		this.cutsceneCache = CutsceneCache(this)
		this.metroSigns.forEach { it.remove() }
		this.metroSigns.clear()

		// Instead of creating individual holograms for each player, we create a global hologram for everyone and update the text using packets
		this.tutorialCutsceneConfig.metroNextStationSignLocations.forEach {
			val location = it.toLocation()
			this.metroSigns.add(
				location.world.spawn(
					location,
					TextDisplay::class.java
				) {
					it.isPersistent = false
					it.backgroundColor = Color.fromARGB(0, 0, 0, 0)
					it.billboard = Display.Billboard.FIXED
					it.transformation = Transformation(
						it.transformation.translation,
						it.transformation.leftRotation,
						org.joml.Vector3f(
							this.tutorialCutsceneConfig.metroNextStationSignScale,
							this.tutorialCutsceneConfig.metroNextStationSignScale,
							this.tutorialCutsceneConfig.metroNextStationSignScale,
						),
						it.transformation.rightRotation,
					)
					it.text(
						textComponent {
							color(NamedTextColor.GOLD)
							content(this@DreamAjuda.tutorialCutsceneConfig.metroNextStationTextDefault.uppercase())
							font(Key.key("sparklypower", "doto"))
						}
					)
				}
			)
		}
	}

	fun startBeginningCutsceneAndTutorial(player: Player, source: StartTutorialSource) {
		val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

		val viaVersion = Via.getAPI()
		val playerVersion = ProtocolVersion.getProtocol(viaVersion.getPlayerVersion(player))
		val isCompatibleWithCutscenes = !player.isBedrockClient && playerVersion.newerThanOrEqualTo(ProtocolVersion.v1_20_5)

		player.healAndFeed()

		for (staff in Bukkit.getOnlinePlayers().asSequence().filter { it.hasPermission("dreamajuda.snooptutorial") }) {
			staff.sendMessage(
				textComponent {
					color(NamedTextColor.GRAY)
					appendTextComponent {
						append("Player ")
					}
					appendTextComponent {
						color(NamedTextColor.AQUA)
						append(player.name)
					}
					appendTextComponent {
						append(" entrou no tutorial! Usando cutscenes? $isCompatibleWithCutscenes Fonte: $source")
					}
				}
			)
		}

		if (!isCompatibleWithCutscenes) {
			// YES, THE TELEPORT MUST HAPPEN BEFORE THE START TUTORIAL CALL
			// "oh is it because there are some checks and-" NO
			// IT IS BECAUSE FOR SOME REASON THE NPC DOESN'T SPAWN FOR BEDROCK CLIENTS IF WE DON'T DO THIS
			//
			// This is the end position of the cutscene, we also NEED to teleport the player to there to cause the chunks to load
			player.teleport(
				Location(
					revampedTutorialIslandWorld,
					-68.49795683082831,
					106.0,
					-85.61882215939532,
					-0.14522988f,
					-0.35526463f
				)
			)

			// If it isn't compatible with cutscenes, we'll just teleport the player to the cutscene end location and start the tutorial
			startTutorial(player, SparklyTutorial.LeaveTheSubway::class)
			return
		}

		val job = launchAsyncThread {
			try {
				if (isLoadingCutscene.contains(player)) {
					player.sendMessage(
						textComponent {
							color(NamedTextColor.RED)
							content("A cutscene está carregando!")
						}
					)
					return@launchAsyncThread
				}

				isLoadingCutscene.add(player)

				val skin = DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesBySparklyPowerUniqueId(player.uniqueId)
				val lorittaSkin = DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesByMojangName("Loritta")!!

				val config = File(this@DreamAjuda.dataFolder, "cutscene_tutorial.yml")
					.readText()
					.let {
						Yaml.default.decodeFromString<SparklyTutorialCutsceneConfig>(it)
					}

				onMainThread {
					// This is the end position of the cutscene, we also NEED to teleport the player to there to cause the chunks to load
					player.teleport(
						Location(
							revampedTutorialIslandWorld,
							-68.49795683082831,
							106.0,
							-85.61882215939532,
							-0.14522988f,
							-0.35526463f
						)
					)

					var entityManager: CutsceneEntityManager? = null
					var gso: SparklyTutorialCutsceneFinalCut.GlobalSceneObjects? = null
					var cutsceneCamera: SparklyCutsceneCamera? = null
					var cutscene: SparklyTutorialCutsceneFinalCut? = null

					try {
						// The tutorial should start AFTER teleporting the player
						startTutorial(player, SparklyTutorial.LeaveTheSubway::class)

						// We need to delay it by one tick to let the chunks to ACTUALLY be loaded, to avoid NPE when attempting to create the GlobalSceneObjects
						// We delay it for 20 ticks to avoid any laggy clients causing issues
						delayTicks(20L)

						entityManager = CutsceneEntityManager(this@DreamAjuda, player)
						gso = SparklyTutorialCutsceneFinalCut.GlobalSceneObjects(
							entityManager,
							player,
							this@DreamAjuda,
							player.world,
							config,
							skin
						)

						cutsceneCamera = SparklyCutsceneCamera(this@DreamAjuda, player)
						cutscene = SparklyTutorialCutsceneFinalCut(
							this@DreamAjuda,
							player,
							cutsceneCamera,
							revampedTutorialIslandWorld,
							config,
							entityManager,
							gso,
							skin,
							lorittaSkin
						)
						cutscene.start()
						cutscene.end(true)
						gso.remove()
					} catch (e: Throwable) {
						logger.log(Level.WARNING, e) { "Something went wrong while trying to play the cutscene!" }

						// If something goes terribly wrong during the cutscene, we'll try reverting the player to the server spawn
						// We won't teleport the player to the spawn, however, because this may be triggered by a PlayerTeleportEvent
						// We don't need to end the tutorial
						cutscene?.end(true)
						gso?.remove()
						player.sendMessage(
							textComponent {
								color(NamedTextColor.RED)
								content("A cutscene foi cancelada!")
							}
						)
					}
				}
			} finally {
				isLoadingCutscene.remove(player)
			}
		}

		cutsceneJobs[player] = job
		job.invokeOnCompletion {
			cutsceneJobs.remove(player, job)
		}
	}

	fun startTutorial(
		player: Player,
		tutorialClazz: KClass<out SparklyTutorial>
	): PlayerTutorial {
		// We don't need to take care that we aren't hiding ourselves because the server already checks it for us
		// We use hideEntity instead of hidePlayer because we don't want to remove the player from the TAB list
		val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

		revampedTutorialIslandWorld.players
			.forEach {
				// Hide all players on this world from that player
				it.hideEntity(this@DreamAjuda, player)

				// And hide everyone to ourselves
				player.hideEntity(this@DreamAjuda, it)
			}

		val activeTutorial = activeTutorials[player]
		activeTutorial?.remove()

		val newTutorial = PlayerTutorial(this, player)
		val sparklyTutorial = tutorialClazz.primaryConstructor!!.call(newTutorial)
		newTutorial.activeTutorial = sparklyTutorial
		activeTutorials[player] = newTutorial
		newTutorial.launchMainThreadTutorialTask {
			sparklyTutorial.onStart()
		}
		return newTutorial
	}

	fun endTutorial(player: Player) {
		val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

		revampedTutorialIslandWorld.players
			.forEach {
				// And now we revert the entity visibilities
				it.showEntity(this@DreamAjuda, player)
				player.showEntity(this@DreamAjuda, it)
			}

		val activeTutorial = activeTutorials[player]
		activeTutorial?.remove()
		activeTutorials.remove(player)
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onChat(e: AsyncPlayerChatEvent) {
		if (e.player.hasPermission("sparklypower.soustaff"))
			return

		if (!e.player.location.isWithinRegion("rules_island"))
			return

		e.isCancelled = true
		e.player.sendTextComponent {
			color(NamedTextColor.RED)
			content("Você precisa ler e aceitar as regras antes de poder conversar no chat!")
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		if (e.player.hasPermission("sparklypower.soustaff"))
			return

		if (!e.player.location.isWithinRegion("rules_island"))
			return

		e.isCancelled = true
		e.player.sendTextComponent {
			color(NamedTextColor.RED)
			content("Você precisa ler e aceitar as regras antes de poder usar comandos!")
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onJoin(e: PlayerJoinEvent) {
		val rulesVersion = e.player.persistentDataContainer.get(RULES_VERSION)

		if (rulesVersion != config.getInt("rules-version")) {
			// New rules, teleport the player!
			for (staff in Bukkit.getOnlinePlayers().asSequence().filter { it.hasPermission("dreamajuda.snooptutorial") }) {
				staff.sendMessage(
					textComponent {
						color(NamedTextColor.GRAY)
						appendTextComponent {
							append("Player ")
						}
						appendTextComponent {
							color(NamedTextColor.AQUA)
							append(e.player.name)
						}
						appendTextComponent {
							append(" foi teletransportado para as regras! Regras do player: $rulesVersion; Regras atuais: ${config.getInt("rules-version")}")
						}
					}
				)
			}

			if (tutorialConfig.enabled) {
				e.player.teleport(Location(Bukkit.getWorld("RevampedTutorialIsland"), -73.5, 117.0, -85.5, -90f, 0f))
			} else {
				e.player.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1100.5, 174.0, 1000.5, 270f, 0f))
			}
		}
	}

	@EventHandler
	fun onRulesApproval(e: PlayerInteractEvent) {
		val clickedBlock = e.clickedBlock ?: return

		if (e.action != Action.LEFT_CLICK_BLOCK && e.action != Action.RIGHT_CLICK_BLOCK)
			return

		if (!clickedBlock.type.name.contains("_SIGN"))
			return

		val sign = clickedBlock.state as Sign
		val isRulesSign = sign.persistentDataContainer.get(IS_RULES_SIGN)
		if (!isRulesSign)
			return

		// The user accepted the rules, yay! Let's update the "rules version"...
		e.player.persistentDataContainer.set(RULES_VERSION, config.getInt("rules-version"))

		if (tutorialConfig.enabled) {
			// And start the tutorial!
			startBeginningCutsceneAndTutorial(e.player, StartTutorialSource.RULES_SIGN)
		} else {
			// And teleport it somewhere else!
			e.player.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1011.5, 100.0, 1000.5, 90f, 0f))
		}
	}

	@EventHandler
	fun onMove(e: PlayerMoveEvent) {
		if (!e.displaced)
			return

		if (e.to.world.name == "TutorialIsland" && 0 >= e.to.y) {
			// If the player falls into the voice, we will teleport them somewhere else!
			// And teleport it somewhere else!
			e.player.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1011.5, 100.0, 1000.5, 90f, 0f))
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	fun onTakeLectern(e: PlayerTakeLecternBookEvent) {
		if (e.player.world.name != "TutorialIsland")
			return

		val book = e.book ?: return

		if (book.type == Material.WRITABLE_BOOK) {
			if (e.player.hasPermission("sparklypower.soustaff"))
				return

			e.isCancelled = true

			e.player.sendMessage("§cEste livro não pode ser removido!")
			return
		}

		e.isCancelled = true

		e.player.inventory.addItem(
			book.clone().meta<BookMeta> {
				this.author(
					textComponent("Pantufa, a mascote do servidor") {
						color(NamedTextColor.GOLD)
					}
				)
			}
		)

		e.player.closeInventory()

		e.player.sendMessage("§aGostou do livro? Eu te dei uma cópia dele para que você possa ler ele no seu cafofo!")
	}

	@EventHandler
	fun onInteract(e: PlayerInteractEntityEvent) {
		val entity = e.rightClicked

		// Faço a MÍNIMA IDEIA porque não tem o §a
		if (e.player.world.name == "TutorialIsland" && (entity.name == "Pantufa" || entity.name == "Loritta" || entity.name == "Gabriela")) {
			openMenu(e.player)
		}
	}

	fun openMenu(player: Player) {
		val menu = createMenu(27, "§6§lAjuda do §4§lSparkly§b§lPower") {
			slot(0, 0) {
				item = ItemStack(Material.CHICKEN_SPAWN_EGG)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.RED)
							decorate(TextDecoration.BOLD)
							content("Pets Queridos e Amigáveis")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 953.5, 100.0, 939.5, 90f, 0f))
				}
			}

			slot(4, 0) {
				item = ItemStack(Material.NETHER_STAR)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.GOLD)
							decorate(TextDecoration.BOLD)
							content("Ilha Principal da Ajuda")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1000.5, 100.0, 1000.5, 270f, 0f))
				}
			}

			slot(8, 0) {
				item = ItemStack(Material.EMERALD)
					.meta<ItemMeta> {
						setCustomModelData(1)

						displayNameWithoutDecorations {
							color(NamedTextColor.GREEN)
							decorate(TextDecoration.BOLD)
							content("Economia e Ostentações")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1000.5, 100.0, 1058.5, 270f, 0f))
				}
			}

			slot(3, 1) {
				item = ItemStack(Material.GOLDEN_SHOVEL)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.YELLOW)
							decorate(TextDecoration.BOLD)
							content("Proteção de Terrenos")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 1000.5, 100.0, 941.5, 270f, 0f))
				}
			}

			slot(4, 1) {
				item = ItemStack(Material.LECTERN)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.GOLD)
							decorate(TextDecoration.BOLD)
							content("Informações Essenciais")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 995.5, 100.0, 1000.5, 90f,  0f))
				}
			}

			slot(5, 1) {
				item = ItemStack(Material.AXOLOTL_BUCKET)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.BLUE)
							decorate(TextDecoration.BOLD)
							content("Registro")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 953.5, 100.0, 1059.5, 0f,0f))
				}
			}

			slot(0, 2) {
				item = ItemStack(Material.PAPER)
					.meta<ItemMeta> {
						setCustomModelData(46)

						displayNameWithoutDecorations {
							color(NamedTextColor.AQUA)
							decorate(TextDecoration.BOLD)
							content("Nossos Itens Inovadores")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 953.5, 100.0, 971.5, 90f, 0f))
				}
			}

			slot(4, 2) {
				item = ItemStack(Material.BOOK)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.DARK_PURPLE)
							decorate(TextDecoration.BOLD)
							content("Regras do SparklyPower")
						}
					}

				onClick {
					Bukkit.dispatchCommand(it, "warp regras")
				}
			}

			slot(8, 2) {
				item = ItemStack(Material.ENCHANTING_TABLE)
					.meta<ItemMeta> {
						displayNameWithoutDecorations {
							color(NamedTextColor.WHITE)
							decorate(TextDecoration.BOLD)
							content("Sistemas do SparklyPower")
						}
					}

				onClick {
					it.teleport(Location(Bukkit.getWorld("TutorialIsland"), 953.5, 100.0, 1029.5, 90f, 0f))
				}
			}
		}

		menu.sendTo(player)
	}
}