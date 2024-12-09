package net.perfectdreams.dreamajuda.cutscenes

import com.destroystokyo.paper.profile.ProfileProperty
import io.papermc.paper.adventure.PaperAdventure
import io.papermc.paper.math.Position
import kotlinx.coroutines.delay
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.perfectdreams.dreamajuda.BypassWGListener
import net.perfectdreams.dreamajuda.CameraPathPoint
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.theatermagic.RecordingPlaybackContext
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.BlockUtils
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.sendPacket
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.set
import org.bukkit.*
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Door
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.entity.Villager
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.scoreboard.Team
import org.bukkit.util.Transformation
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class SparklyTutorialCutsceneFinalCut(
    m: DreamAjuda,
    player: Player,
    cutsceneCamera: SparklyCutsceneCamera,
    val world: World,
    val config: SparklyTutorialCutsceneConfig,
    val entityManager: CutsceneEntityManager,
    val globalSceneObjects: GlobalSceneObjects,
    val playerSkin: ProfileProperty?,
    val lorittaSkin: ProfileProperty?,
) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, entityManager) {
    override suspend fun start() {
        runCutscene(TheBeginning(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin), false)
        runCutscene(BoringOffice(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin), false)
        runCutscene(LookAtPhone(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin), false)
        runCutscene(LorittaMessage(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin, lorittaSkin), false)
        runCutscene(HomeSweetHome(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin), false)
        runCutscene(MiningStuffz(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin), false)
        runCutscene(LifetimeFriends(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin), false)
        runCutscene(LeaveYourMark(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin), false)
        // Fazer amigos para a vida inteira...
        // E deixar a sua marca no servidor

        runCutscene(NextStationFocus(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin), false)
        // runCutscene(BlackWallFade(m, player, cutsceneCamera, cutsceneEntityManager, world, config, globalSceneObjects, playerSkin), false)
    }

    class GlobalSceneObjects(
        val entityManager: CutsceneEntityManager,
        val player: Player,
        val m: DreamAjuda,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        playerSkin: ProfileProperty?,
    ) {
        val npc = entityManager.spawnFakePlayer(
            Location(world, -68.39515605857557, 106.5, -85.89218845900189, 0.0f, 0f),
            player.name,
            playerSkin?.let { SkinTexture(it.value, it.signature!!) }
        )

        val npcChairEntity = entityManager.spawnEntity(
            config.playerMetroChairSittingLocation.toLocation(world),
            TextDisplay::class.java
        ) {
            it.addPassenger(npc.getEntity()!!)
        }

        fun remove() {
            npcChairEntity.remove()
            npc.remove()
        }
    }

    class TheBeginning(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            val blocksToBeChanged = mutableMapOf<Position, BlockData>()

            for (blackWall in config.blackWalls) {
                val blocks = BlockUtils.getBlocksFromTwoPositions(world, blackWall.area1.toPosition(), blackWall.area2.toPosition())

                blocks.forEach {
                    blocksToBeChanged[Position.block(it.location)] = Bukkit.createBlockData(Material.BLACK_CONCRETE)
                }
            }

            for (metroDoorBottom in config.metroDoors) {
                val blockBottom = world.getBlockAt(metroDoorBottom.toLocation(world))
                val blockDataBottom = blockBottom.blockData as Door
                blockDataBottom.isOpen = true

                blocksToBeChanged[Position.block(blockBottom.location)] = blockDataBottom

                val blockTop = world.getBlockAt(metroDoorBottom.toLocation(world).add(0.0, 1.0, 0.0))
                val blockDataTop = blockTop.blockData as Door
                blockDataTop.isOpen = true

                blocksToBeChanged[Position.block(blockTop.location)] = blockDataTop
            }

            player.sendMultiBlockChange(blocksToBeChanged)

            val originalCamera = Location(world, -70.59881234126554, 106.6214850545078, -62.31686249472241, -179.51624f, -11.665535f)

            player.playSound(
                config.metroSoundSourceLocation.toLocation(world),
                config.metroSoundKey,
                config.metroSoundVolume,
                1f
            )

            val targetCamera = Location(world, -70.58570933902557, 106.6214850545078, -80.4733926209905, -179.57544f, -10.866125f)
            val targetCamera2 = Location(world, -69.03694868977868, 107.4, -84.7289952581889, -152.9267f, 0.91783327f)

            val cameraMove = m.launchMainThread {
                cutsceneCamera.easeCamera(
                    m,
                    originalCamera,
                    listOf(
                        CameraPathPoint(
                            targetCamera,
                            120
                        ),
                        CameraPathPoint(
                            targetCamera2,
                            40
                        )
                    )
                ).join()
            }

            player.showTitle(
                Title.title(
                    textComponent {
                        color(NamedTextColor.BLACK)
                        content("\uE292")
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(0), Ticks.duration(20), Ticks.duration(60))
                )
            )

            delayTicks(80L)

            player.showTitle(
                Title.title(
                    textComponent {
                        decorate(TextDecoration.BOLD)

                        appendTextComponent {
                            content("Sparkly")
                            color(TextColor.color(237, 46, 22))
                        }

                        appendTextComponent {
                            content("Power")
                            color(TextColor.color(1, 235, 247))
                        }
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            cameraMove.join()

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        color(NamedTextColor.GOLD)
                        content("Este é você...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(60L)

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        color(NamedTextColor.GOLD)
                        // content("E resolveu se mudar para outro lugar...")
                        // content("Decidiu começar do zero em um lugar diferente...")
                        content("Buscando uma nova chance, em um novo lugar...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(60L)

            player.playSound(player, "sparklypower:general.wind_noises1", 0.3f, 1.1f)

            player.showTitle(
                Title.title(
                    textComponent {
                        color(NamedTextColor.WHITE)
                        content("\uE292")
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(5), Ticks.duration(0), Ticks.duration(20))
                )
            )

            delayTicks(5L)
        }
    }

    class BoringOffice(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?,
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            val npc = cutsceneEntityManager.spawnFakePlayer(
                config.boringOfficePlayerChairSittingLocation.toLocation(world),
                player.name,
                playerSkin?.let { SkinTexture(it.value, it.signature!!) }
            )

            val npcChairEntity = cutsceneEntityManager.spawnEntity(
                config.boringOfficePlayerChairSittingLocation.toLocation(world),
                TextDisplay::class.java
            ) {
                it.addPassenger(npc.getEntity()!!)
            }

            val officeAmbianceJob = m.launchMainThread {
                while (true) {
                    player.playSound(
                        npcChairEntity,
                        "sparklypower:general.office_ambiance",
                        0.7f,
                        1f
                    )
                    delay(4.seconds)
                }
            }

            cutsceneCamera.setCameraLocation(config.boringOfficeCameraStartLocation.toLocation(world), 0)
            cutsceneCamera.setCameraLocation(config.boringOfficeCameraFinishLocation.toLocation(world), 85)

            val villagers = config.boringOfficeVillagersLocations.map {
                cutsceneEntityManager.spawnEntity(
                    it.toLocation(world),
                    Villager::class.java
                ) {
                    it.persistentDataContainer.set(BypassWGListener.BYPASS_WORLDGUARD_MOB_SPAWNING_DENY_KEY, true)
                    it.setAI(false)
                }
            }

            val r = Random(0)

            val villagerNoisesJob = m.launchMainThread {
                while (true) {
                    val villager = villagers.random(r)
                    player.playSound(
                        villager,
                        if (r.nextBoolean()) "minecraft:entity.villager.celebrate" else "minecraft:entity.villager.no",
                        0.2f,
                        r.nextDouble(1.0, 1.3).toFloat()
                    )

                    delayTicks(17L)
                }
            }

            delayTicks(20L)

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        // content("Você finalmente largou aquele emprego CLT tosco que você tinha...")
                        // content("Deixando a velha rotina para trás...")
                        color(NamedTextColor.GOLD)
                        content("Deixando para trás o que não te fazia feliz...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(60L)

            player.playSound(player, "sparklypower:general.wind_noises1", 0.3f, 0.95f)

            player.showTitle(
                Title.title(
                    textComponent {
                        color(NamedTextColor.WHITE)
                        content("\uE292")
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(5), Ticks.duration(0), Ticks.duration(20))
                )
            )

            delayTicks(5L)

            npcChairEntity.remove()
            npc.remove()
            villagerNoisesJob.cancel()
            villagers.forEach {
                it.remove()
            }
            officeAmbianceJob.cancel()
        }
    }

    class LookAtPhone(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?,
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            cutsceneCamera.setCameraLocation(Location(world, -69.2227185770231, 107.77459719593291, -86.27805483484802, -30.466305f, 55f), 0)

            globalSceneObjects.npc.equipment.setItem(
                EquipmentSlot.HAND,
                ItemStack.of(Material.PAPER)
                    .meta<ItemMeta> {
                        this.setCustomModelData(223)
                    }
            )

            delayTicks(20L)

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        // content("Tudo por causa da mensagem daquela garota...")
                        color(NamedTextColor.GOLD)
                        content("Tudo começou por causa dela...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(80L)

            // This has a longer fade intentionally!!!
            player.playSound(player, "sparklypower:general.wind_noises1", 0.3f, 0.5f)

            player.showTitle(
                Title.title(
                    textComponent {
                        color(NamedTextColor.WHITE)
                        content("\uE292")
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(5), Ticks.duration(0), Ticks.duration(40))
                )
            )

            delayTicks(5L)
        }
    }

    class LorittaMessage(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?,
        val lorittaSkin: ProfileProperty?,
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            val display = createLorittaMessage()

            val npc = spawnLorittaMessageSelfNPC()

            cutsceneCamera.easeCamera(
                m,
                Location(world, -75.0, 89.75, -99.5, 180.0f, 0.0f),
                listOf(
                    CameraPathPoint(
                        Location(world, -75.0, 89.75, -95.5, 180.0f, 0.0f),
                        90
                    )
                )
            )

            delayTicks(40L)

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        color(NamedTextColor.GOLD)
                        content("A garota que só fala de sonhos...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            player.playSound(player, "sparklypower:general.rapaiz", 1000f, 1f)

            delayTicks(80L)

            val npc2 = cutsceneEntityManager.spawnFakePlayer(
                Location(
                    world,
                    -75.02086775074017,
                    87.0,
                    -99.67983456303703,
                    0.21942139f,
                    2.9611778f
                ),
                "Loritta",
                lorittaSkin?.let { SkinTexture(it.value, it.signature!!) }
            )

            player.playSound(npc2.location, "sparklypower:general.discord_message", 1000f, 1f)

            player.spawnParticle(Particle.HAPPY_VILLAGER, npc2.location, 25, 0.25, 0.25, 0.25)
            repeat(10) {
                npc2.teleport(npc2.location.add(0.0, -0.2, 0.0))
                delayTicks(1L)
            }
            cutsceneCamera.setCameraLocation(Location(world, -76.71498220751405, 86.66183149803553, -96.25794072057725, -158.19145f, -3.6714854f), 0)

            val context = RecordingPlaybackContext(
                m,
                player,
                m.cutsceneCache.lorittaSpeaks,
                player.world
            ) {
                npc2
            }

            val playback = context.startPlayback {}
            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        color(NamedTextColor.GOLD)
                        content("Ela te contou sobre um lugar incrível...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            // cutscene.setCameraLocation(Location(world, -76.74684348754286, 85.81913897026025, -94.47603241905854, -146.97168f, 0.94750345f))
            delayTicks(60L)
            cutsceneCamera.setCameraLocation(config.lorittaMessageYouAlwaysDreamedOfCameraStartLocation.toLocation(world), 0)
            cutsceneCamera.setCameraLocation(config.lorittaMessageYouAlwaysDreamedOfCameraFinishLocation.toLocation(world), 65)

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        color(NamedTextColor.GOLD)
                        content("Um lugar do jeito que você sempre sonhou...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(80L)

            player.playSound(player, "sparklypower:general.wind_noises1", 0.3f, 1.02f)

            player.showTitle(
                Title.title(
                    textComponent {
                        color(NamedTextColor.WHITE)
                        content("\uE292")
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(5), Ticks.duration(0), Ticks.duration(20))
                )
            )

            delayTicks(5L)

            display.remove()
            npc.remove()
            npc2.remove()
            playback.cancel()
        }

        fun createLorittaMessage(): TextDisplay {
            return cutsceneEntityManager.spawnEntity(
                Location(world, -75.0, 90.0, -99.69, 0f, 0f),
                TextDisplay::class.java
            ) {
                it.backgroundColor = Color.fromARGB(0, 0, 0, 0)
                it.billboard = Display.Billboard.FIXED
                it.transformation = Transformation(
                    it.transformation.translation,
                    it.transformation.leftRotation,
                    org.joml.Vector3f(
                        0.2f,
                        0.2f,
                        0.2f
                    ),
                    it.transformation.rightRotation,
                )

                it.isSeeThrough = false
                it.lineWidth = 9999
                it.isPersistent = false
                it.brightness = Display.Brightness(15, 15)
                it.text(textComponent {
                    color(NamedTextColor.WHITE)
                    font(Key.key("sparklypower", "stuff"))
                    content("abcd")
                })
            }
        }

        fun spawnLorittaMessageSelfNPC(): SparklyNPC {
            return cutsceneEntityManager.spawnFakePlayer(
                Location(world, -74.99907710514692, 85.0, -96.49933552549739, -179.68922f, 0.029606178f),
                player.name,
                playerSkin?.let { SkinTexture(it.value, it.signature!!) }
            )
        }
    }

    class HomeSweetHome(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            val npc = cutsceneEntityManager.spawnFakePlayer(
                Location(
                    world,
                    -75.02086775074017,
                    87.0,
                    -99.67983456303703,
                    0.21942139f,
                    2.9611778f
                ),
                player.name,
                playerSkin?.let { SkinTexture(it.value, it.signature!!) }
            )

            val context = RecordingPlaybackContext(
                m,
                player,
                m.cutsceneCache.leavingHouse,
                player.world
            ) {
                npc
            }

            val job = context.startPlayback {}

            cutsceneCamera.setCameraLocation(Location(world, -75.0, 77.8, -104.5, -90f, 0f), 0)

            delayTicks(20L)

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        // content("Onde até quem começa do zero...")
                        color(NamedTextColor.GOLD)
                        content("Um lugar cheio de oportunidades...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(80L)

            player.playSound(player, "sparklypower:general.wind_noises1", 0.3f, 0.98f)

            player.showTitle(
                Title.title(
                    textComponent {
                        color(NamedTextColor.WHITE)
                        content("\uE292")
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(5), Ticks.duration(0), Ticks.duration(20))
                )
            )

            delayTicks(5L)
            job.cancel()
            npc.remove()
        }
    }

    class MiningStuffz(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            cutsceneCamera.setCameraLocation(Location(world, -76.25097529609799, 86.75533307521188, -72.8356438421399, -134.244f, 7.7575636f), 0)

            val npc = cutsceneEntityManager.spawnFakePlayer(
                Location(
                    world,
                    -75.02086775074017,
                    87.0,
                    -99.67983456303703,
                    0.21942139f,
                    2.9611778f
                ),
                player.name,
                playerSkin?.let { SkinTexture(it.value, it.signature!!) }
            )

            val context = RecordingPlaybackContext(
                m,
                player,
                m.cutsceneCache.mining,
                player.world
            ) {
                npc
            }

            val playback = context.startPlayback {}

            delayTicks(20L)

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        color(NamedTextColor.GOLD)
                        content("Onde cada bloco pode ser o começo de algo grandioso...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(80L)

            player.playSound(player, "sparklypower:general.wind_noises1", 0.3f, 1.1f)

            player.showTitle(
                Title.title(
                    textComponent {
                        color(NamedTextColor.WHITE)
                        content("\uE292")
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(5), Ticks.duration(0), Ticks.duration(20))
                )
            )

            delayTicks(5L)
            playback.cancel()
            npc.remove()
        }
    }

    class LifetimeFriends(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            cutsceneCamera.setCameraLocation(config.lifetimeFriends.cameraLocation.toLocation(world), 0)

            val npc = cutsceneEntityManager.spawnFakePlayer(
                config.leaveYourMark.playerChairSittingLocation.toLocation(world),
                player.name,
                config.lifetimeFriends.skin2?.let { SkinTexture(it.value, it.signature) }
            )

            val npc2 = cutsceneEntityManager.spawnFakePlayer(
                config.leaveYourMark.playerChairSittingLocation.toLocation(world),
                player.name,
                playerSkin?.let { SkinTexture(it.value, it.signature!!) }
            )

            val npc3 = cutsceneEntityManager.spawnFakePlayer(
                config.leaveYourMark.playerChairSittingLocation.toLocation(world),
                player.name,
                config.lifetimeFriends.skin3?.let { SkinTexture(it.value, it.signature) }
            )

            val npc4 = cutsceneEntityManager.spawnFakePlayer(
                config.leaveYourMark.playerChairSittingLocation.toLocation(world),
                player.name,
                config.lifetimeFriends.skin4?.let { SkinTexture(it.value, it.signature) }
            )

            npc.setNameplateVisibility(Team.OptionStatus.NEVER)
            npc3.setNameplateVisibility(Team.OptionStatus.NEVER)
            npc4.setNameplateVisibility(Team.OptionStatus.NEVER)

            val context1 = RecordingPlaybackContext(
                m,
                player,
                m.cutsceneCache.build1,
                player.world
            ) {
                npc
            }

            val playback1 = context1.startPlayback {}

            val context2 = RecordingPlaybackContext(
                m,
                player,
                m.cutsceneCache.build2,
                player.world
            ) {
                npc2
            }

            val playback2 = context2.startPlayback {}

            val context3 = RecordingPlaybackContext(
                m,
                player,
                m.cutsceneCache.build3,
                player.world
            ) {
                npc3
            }

            val playback3 = context3.startPlayback {}

            val context4 = RecordingPlaybackContext(
                m,
                player,
                m.cutsceneCache.build4,
                player.world
            ) {
                npc4
            }

            val playback4 = context4.startPlayback {}

            delayTicks(20L)

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        color(NamedTextColor.GOLD)
                        content("Onde laços duradouros começam...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(80L)

            player.playSound(player, "sparklypower:general.wind_noises1", 0.3f, 0.99f)

            player.showTitle(
                Title.title(
                    textComponent {
                        color(NamedTextColor.WHITE)
                        content("\uE292")
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(5), Ticks.duration(0), Ticks.duration(20))
                )
            )

            delayTicks(5L)
            npc.remove()
            npc2.remove()
            npc3.remove()
            npc4.remove()
            playback1.cancel()
            playback2.cancel()
            playback3.cancel()
            playback4.cancel()
        }
    }

    class LeaveYourMark(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            cutsceneCamera.setCameraLocation(config.leaveYourMark.cameraStartLocation.toLocation(world), 0)

            val npc = cutsceneEntityManager.spawnFakePlayer(
                config.leaveYourMark.playerChairSittingLocation.toLocation(world),
                player.name,
                playerSkin?.let { SkinTexture(it.value, it.signature!!) }
            )

            val npcChairEntity = cutsceneEntityManager.spawnEntity(
                config.leaveYourMark.playerChairSittingLocation.toLocation(world),
                TextDisplay::class.java
            ) {
                it.addPassenger(npc.getEntity()!!)
            }

            cutsceneCamera.setCameraLocation(config.leaveYourMark.cameraStartLocation.toLocation(world), 0)
            cutsceneCamera.setCameraLocation(config.leaveYourMark.cameraFinishLocation.toLocation(world), 105)

            delayTicks(20L)

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        color(NamedTextColor.GOLD)
                        content("Onde faíscas começam a brilhar...")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(80L)

            player.playSound(player, "sparklypower:general.wind_noises1", 0.3f, 1f)

            player.showTitle(
                Title.title(
                    textComponent {
                        color(NamedTextColor.WHITE)
                        content("\uE292")
                    },
                    textComponent {},
                    Title.Times.times(Ticks.duration(5), Ticks.duration(0), Ticks.duration(20))
                )
            )

            delayTicks(5L)
            npc.remove()
            npcChairEntity.remove()
        }
    }

    class NextStationFocus(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            cutsceneCamera.setCameraLocation(config.metroNextStationCameraLocation.toLocation(world), 0)

            player.playSound(config.metroNextStationCameraLocation.toLocation(world), "sparklypower:general.metro_paraiso", 1000f, 1f)

            val nextStationJob = m.launchMainThread {
                val lines = createMarqueeSequence(config.metroNextStationText.uppercase(), 23) + config.metroNextStationTextDefault.uppercase()

                for (line in lines) {
                    val displayEntityText = textComponent {
                        color(NamedTextColor.GOLD)
                        content(line)
                        font(Key.key("sparklypower", "doto"))
                    }
                    val nmsDisplayEntityText = PaperAdventure.asVanilla(displayEntityText)

                    for (entity in m.metroSigns) {
                        val metadata = ClientboundSetEntityDataPacket(
                            entity.entityId,
                            listOf(
                                SynchedEntityData.DataValue(
                                    23,
                                    EntityDataSerializers.COMPONENT,
                                    nmsDisplayEntityText
                                )
                            )
                        )
                        player.sendPacket(metadata)
                    }

                    delayTicks(3L)
                }
            }

            nextStationJob.join()

            val blocksToBeChanged = mutableMapOf<Position, BlockData>()

            for (blackWall in config.blackWalls) {
                val blocks = BlockUtils.getBlocksFromTwoLocations(blackWall.area1.toLocation(world), blackWall.area2.toLocation(world))

                blocks.forEach {
                    blocksToBeChanged[Position.block(it.location)] = it.blockData
                }
            }

            for (metroDoorBottom in config.metroDoors) {
                // We want to resend the block as is
                val blockBottom = world.getBlockAt(metroDoorBottom.toLocation(world))
                val blockDataBottom = blockBottom.blockData as Door

                blocksToBeChanged[Position.block(blockBottom.location)] = blockDataBottom

                val blockTop = world.getBlockAt(metroDoorBottom.toLocation(world).add(0.0, 1.0, 0.0))
                val blockDataTop = blockTop.blockData as Door

                blocksToBeChanged[Position.block(blockTop.location)] = blockDataTop
            }

            player.sendMultiBlockChange(blocksToBeChanged)

            delayTicks(20L)

            cutsceneCamera.setCameraLocation(
                config.playerCutsceneFinishLocation.toLocation(world)
                    .add(0.0, 1.62, 0.0),
                100
            )

            player.showTitle(
                Title.title(
                    textComponent {},
                    textComponent {
                        color(NamedTextColor.GOLD)
                        content("Parece que chegou a sua hora de brilhar.")
                    },
                    Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                )
            )

            delayTicks(120L)
        }

        fun createMarqueeSequence(input: String, width: Int): List<String> {
            if (width <= 0) return emptyList() // Handle invalid width

            val paddedInput = input.padEnd(input.length + width, ' ') // Add spaces at the end
            val sequence = mutableListOf<String>()

            for (i in 0..paddedInput.length - 1) {
                val visiblePart = paddedInput.take(i).takeLast(width) // Extract only the visible width
                sequence.add(visiblePart.padStart(width)) // Right-align with padding
            }

            return sequence
        }
    }

    class BlackWallFade(
        m: DreamAjuda,
        player: Player,
        cutsceneCamera: SparklyCutsceneCamera,
        cutsceneEntityManager: CutsceneEntityManager,
        val world: World,
        val config: SparklyTutorialCutsceneConfig,
        val globalSceneObjects: GlobalSceneObjects,
        val playerSkin: ProfileProperty?
    ) : SparklyCutsceneFinalCut(m, player, cutsceneCamera, cutsceneEntityManager) {
        override suspend fun start() {
            for (blackWall in config.blackWalls) {
                val blocks = BlockUtils.getBlocksFromTwoLocations(blackWall.area1.toLocation(world), blackWall.area2.toLocation(world))

                player.sendMultiBlockChange(
                    blocks.associate {
                        Position.block(it.location) to Bukkit.createBlockData(Material.BLACK_CONCRETE)
                    }
                )
            }

            repeat(35) { zOffset ->
                for (blackWall in config.blackWalls) {
                    val blocks = BlockUtils.getBlocksFromTwoLocations(
                        blackWall.area1.toLocation(world),
                        blackWall.area2.toLocation(world)
                    ).filter {
                        it.z >= (-57 - zOffset)
                    }

                    player.sendMultiBlockChange(
                        blocks.associate {
                            Position.block(it.location) to world.getBlockAt(it.location).blockData
                        }
                    )

                    delayTicks(2L)
                }
            }
        }

        fun createMarqueeSequence(input: String, width: Int): List<String> {
            if (width <= 0) return emptyList() // Handle invalid width

            val paddedInput = input.padEnd(input.length + width, ' ') // Add spaces at the end
            val sequence = mutableListOf<String>()

            for (i in 0..paddedInput.length - 1) {
                val visiblePart = paddedInput.take(i).takeLast(width) // Extract only the visible width
                sequence.add(visiblePart.padStart(width)) // Right-align with padding
            }

            return sequence
        }
    }
}