package net.perfectdreams.dreamajuda.commands

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import com.mojang.serialization.Dynamic
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.kyori.adventure.util.Ticks
import net.kyori.adventure.util.TriState
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.NbtException
import net.minecraft.nbt.ReportedNbtException
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.Main
import net.minecraft.server.MinecraftServer
import net.minecraft.server.WorldLoader.DataLoadContext
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.dedicated.DedicatedServerProperties.WorldDimensionData
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.GsonHelper
import net.minecraft.util.datafix.DataFixers
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.ai.village.VillageSiege
import net.minecraft.world.entity.npc.CatSpawner
import net.minecraft.world.entity.npc.WanderingTraderSpawner
import net.minecraft.world.level.*
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.PatrolSpawner
import net.minecraft.world.level.levelgen.PhantomSpawner
import net.minecraft.world.level.levelgen.WorldDimensions
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.storage.LevelStorageSource
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess
import net.minecraft.world.level.storage.LevelSummary
import net.minecraft.world.level.storage.PrimaryLevelData
import net.minecraft.world.level.validation.ContentValidationException
import net.perfectdreams.dreamajuda.*
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.BlockUtils
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcore.utils.toTextComponent
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.block.data.type.Door
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.generator.CraftWorldInfo
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.generator.BiomeParameterPoint
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Transformation
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.time.measureTimedValue

class Tutorial2Executor(val m: DreamAjuda) : SparklyCommandExecutor() {
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

    fun createWorld(creator: WorldCreator?): World? {
        val craftServer = Bukkit.getServer() as CraftServer
        val console = DedicatedServer.getServer() as DedicatedServer

        Preconditions.checkState(
            console.getAllLevels().iterator().hasNext(),
            "Cannot create additional worlds on STARTUP"
        )
        //Preconditions.checkState(!this.console.isIteratingOverLevels, "Cannot create a world while worlds are being ticked"); // Paper - Cat - Temp disable. We'll see how this goes.
        Preconditions.checkArgument(creator != null, "WorldCreator cannot be null")

        // var s = System.currentTimeMillis()

        val name = creator!!.name()
        var generator = creator.generator()
        var biomeProvider = creator.biomeProvider()
        val folder: File = File(craftServer.getWorldContainer(), name)
        val world = craftServer.getWorld(name)

        // println("#1 " + (System.currentTimeMillis() - s) + "ms")

        // Paper start
        val worldByKey = craftServer.getWorld(creator.key())
        if (world != null || worldByKey != null) {
            if (world === worldByKey) {
                return world
            }
            throw IllegalArgumentException("Cannot create a world with key " + creator.key() + " and name " + name + " one (or both) already match a world that exists")
        }
        // Paper end
        // println("#2 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        if (folder.exists()) {
            Preconditions.checkArgument(folder.isDirectory, "File (%s) exists and isn't a folder", name)
        }

        // println("#3 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        if (generator == null) {
            generator = craftServer.getGenerator(name)
        }

        // println("#4 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        if (biomeProvider == null) {
            biomeProvider = craftServer.getBiomeProvider(name)
        }

        // println("#5 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()
        val actualDimension: ResourceKey<LevelStem?> = when(creator.environment()) {
            World.Environment.NORMAL -> LevelStem.OVERWORLD
            World.Environment.NETHER -> LevelStem.NETHER
            World.Environment.THE_END -> LevelStem.END
            else -> throw java.lang.IllegalArgumentException("Illegal dimension ("+creator.environment()+")")
        }
        // println("#6 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        val worldSession: LevelStorageAccess
        try {
            worldSession = LevelStorageSource.createDefault(craftServer.worldContainer.toPath())
                .validateAndCreateAccess(name, actualDimension)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        } catch (ex: ContentValidationException) {
            throw RuntimeException(ex)
        }

        // println("#7 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        var dynamic: Dynamic<*>?
        if (worldSession.hasWorldData()) {
            var worldinfo: LevelSummary

            try {
                dynamic = worldSession.dataTag
                worldinfo = worldSession.getSummary(dynamic)
            } catch (ioexception: NbtException) {
                val convertable_b = worldSession.getLevelDirectory()

                MinecraftServer.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception)
                MinecraftServer.LOGGER.info("Attempting to use fallback")

                try {
                    dynamic = worldSession.dataTagFallback
                    worldinfo = worldSession.getSummary(dynamic)
                } catch (ioexception1: NbtException) {
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {}",
                        convertable_b.oldDataFile(),
                        ioexception1
                    )
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                        convertable_b.dataFile(),
                        convertable_b.oldDataFile()
                    )
                    return null
                } catch (ioexception1: ReportedNbtException) {
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {}",
                        convertable_b.oldDataFile(),
                        ioexception1
                    )
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                        convertable_b.dataFile(),
                        convertable_b.oldDataFile()
                    )
                    return null
                } catch (ioexception1: IOException) {
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {}",
                        convertable_b.oldDataFile(),
                        ioexception1
                    )
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                        convertable_b.dataFile(),
                        convertable_b.oldDataFile()
                    )
                    return null
                }

                worldSession.restoreLevelDataFromOld()
            } catch (ioexception: ReportedNbtException) {
                val convertable_b = worldSession.getLevelDirectory()

                MinecraftServer.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception)
                MinecraftServer.LOGGER.info("Attempting to use fallback")

                try {
                    dynamic = worldSession.dataTagFallback
                    worldinfo = worldSession.getSummary(dynamic)
                } catch (ioexception1: NbtException) {
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {}",
                        convertable_b.oldDataFile(),
                        ioexception1
                    )
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                        convertable_b.dataFile(),
                        convertable_b.oldDataFile()
                    )
                    return null
                } catch (ioexception1: ReportedNbtException) {
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {}",
                        convertable_b.oldDataFile(),
                        ioexception1
                    )
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                        convertable_b.dataFile(),
                        convertable_b.oldDataFile()
                    )
                    return null
                } catch (ioexception1: IOException) {
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {}",
                        convertable_b.oldDataFile(),
                        ioexception1
                    )
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                        convertable_b.dataFile(),
                        convertable_b.oldDataFile()
                    )
                    return null
                }

                worldSession.restoreLevelDataFromOld()
            } catch (ioexception: IOException) {
                val convertable_b = worldSession.getLevelDirectory()

                MinecraftServer.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception)
                MinecraftServer.LOGGER.info("Attempting to use fallback")

                try {
                    dynamic = worldSession.dataTagFallback
                    worldinfo = worldSession.getSummary(dynamic)
                } catch (ioexception1: NbtException) {
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {}",
                        convertable_b.oldDataFile(),
                        ioexception1
                    )
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                        convertable_b.dataFile(),
                        convertable_b.oldDataFile()
                    )
                    return null
                } catch (ioexception1: ReportedNbtException) {
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {}",
                        convertable_b.oldDataFile(),
                        ioexception1
                    )
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                        convertable_b.dataFile(),
                        convertable_b.oldDataFile()
                    )
                    return null
                } catch (ioexception1: IOException) {
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {}",
                        convertable_b.oldDataFile(),
                        ioexception1
                    )
                    MinecraftServer.LOGGER.error(
                        "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                        convertable_b.dataFile(),
                        convertable_b.oldDataFile()
                    )
                    return null
                }

                worldSession.restoreLevelDataFromOld()
            }

            if (worldinfo.requiresManualConversion()) {
                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted")
                return null
            }

            if (!worldinfo.isCompatible) {
                MinecraftServer.LOGGER.info("This world was created by an incompatible version.")
                return null
            }
        } else {
            dynamic = null
        }

        // println("#8 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        val hardcore = creator.hardcore()

        val worlddata: PrimaryLevelData
        val worldloader_a: DataLoadContext = console.worldLoader
        var iregistrycustom_dimension = worldloader_a.datapackDimensions()
        var iregistry = iregistrycustom_dimension.lookupOrThrow(Registries.LEVEL_STEM)
        if (dynamic != null) {
            val leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(
                dynamic,
                worldloader_a.dataConfiguration(),
                iregistry,
                worldloader_a.datapackWorldgen()
            )

            worlddata = leveldataanddimensions.worldData() as PrimaryLevelData
            iregistrycustom_dimension = leveldataanddimensions.dimensions().dimensionsRegistryAccess()
        } else {
            val worldoptions = WorldOptions(creator.seed(), creator.generateStructures(), false)
            val worlddimensions: WorldDimensions

            val properties = WorldDimensionData(
                GsonHelper.parse(
                    if ((creator.generatorSettings().isEmpty())) "{}" else creator.generatorSettings()
                ), creator.type().name.lowercase()
            )

            val worldsettings = LevelSettings(
                name,
                GameType.byId(craftServer.getDefaultGameMode().getValue()),
                hardcore,
                Difficulty.EASY,
                false,
                GameRules(worldloader_a.dataConfiguration().enabledFeatures()),
                worldloader_a.dataConfiguration()
            )
            worlddimensions = properties.create(worldloader_a.datapackWorldgen())

            val worlddimensions_b = worlddimensions.bake(iregistry)
            val lifecycle = worlddimensions_b.lifecycle().add(worldloader_a.datapackWorldgen().allRegistriesLifecycle())

            worlddata =
                PrimaryLevelData(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle)
            iregistrycustom_dimension = worlddimensions_b.dimensionsRegistryAccess()
        }
        // println("#9 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()
        iregistry = iregistrycustom_dimension.lookupOrThrow(Registries.LEVEL_STEM)
        worlddata.customDimensions = iregistry
        worlddata.checkName(name)
        worlddata.setModdedInfo(
            console.getServerModName(),
            console.getModdedStatus().shouldReportAsModified()
        )
        // println("#10 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        if (console.options.has("forceUpgrade")) {
            Main.forceUpgrade(worldSession, DataFixers.getDataFixer(), console.options.has("eraseCache"),
                { true }, iregistrycustom_dimension, console.options.has("recreateRegionFiles")
            )
        }

        // println("#11 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        val j = BiomeManager.obfuscateSeed(worlddata.worldGenOptions().seed()) // Paper - use world seed
        val list: List<CustomSpawner> = ImmutableList.of(
            PhantomSpawner(),
            PatrolSpawner(),
            CatSpawner(),
            VillageSiege(),
            WanderingTraderSpawner(worlddata)
        )
        val worlddimension = iregistry.getValue(actualDimension)

        // println("#12 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        val worldInfo: WorldInfo = CraftWorldInfo(
            worlddata, worldSession, creator.environment(), worlddimension!!.type().value(), worlddimension.generator(),
            console.registryAccess()
        ) // Paper - Expose vanilla BiomeProvider from WorldInfo
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo)
        }

        // println("#13 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        val worldKey: ResourceKey<Level>
        val levelName: String = console.getProperties().levelName
        worldKey = if (name == levelName + "_nether") {
            Level.NETHER
        } else if (name == levelName + "_the_end") {
            Level.END
        } else {
            ResourceKey.create(
                Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(
                    creator.key().namespace(), creator.key().value()
                )
            )
        }

        // println("#14 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        // If set to not keep spawn in memory (changed from default) then adjust rule accordingly
        if (creator.keepSpawnLoaded() == TriState.FALSE) { // Paper
            worlddata.gameRules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS)[0] = null
        }
        val s = System.currentTimeMillis()
        // TODO: we could move this to a async task... maybe, the only call that could cause issues is the world add at the end
        //  This would require some SparklyPaper changes
        val internal = ServerLevel(
            console,
            console.executor,
            worldSession,
            worlddata,
            worldKey,
            worlddimension,
            console.progressListenerFactory.create(worlddata.gameRules.getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)),
            worlddata.isDebugWorld,
            j,
            if (creator.environment() == World.Environment.NORMAL) list else ImmutableList.of<CustomSpawner>(),
            true,
            console.overworld().getRandomSequences(),
            creator.environment(),
            generator,
            biomeProvider
        )
        println("Took ${System.currentTimeMillis() - s}ms to create!")

        // println("#15 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        /* if (!(craftServer.worlds.containsKey(name.lowercase()))) {
            return null
        } */

        // println("#16 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        console.addLevel(internal) // Paper - Put world into worldlist before initing the world; move up
        // println("#17 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        console.initWorld(internal, worlddata, worlddata, worlddata.worldGenOptions())
        // println("#18 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        internal.setSpawnSettings(true)

        // Paper - Put world into worldlist before initing the world; move up
        // println("#19 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()
        // console.prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal)
        // println("#20 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        // Paper - rewrite chunk system
        // pluginManager.callEvent(WorldLoadEvent(internal.world))
        // println("#21 " + (System.currentTimeMillis() - s) + "ms")
        // s = System.currentTimeMillis()

        return internal.world
    }

    class DreamEmptyWorldGenerator : JavaPlugin() {
        private val generator = EmptyWorldGenerator()

        override fun getDefaultWorldGenerator(worldName: String, id: String?) = generator

        class EmptyWorldGenerator : ChunkGenerator() {
            override fun getFixedSpawnLocation(world: World, random: Random) = Location(world, -45.2, 106.0, -59.2)
        }

        class EmptyBiomeProvider : BiomeProvider() {
            override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome {
                return Biome.PLAINS
            }

            override fun getBiomes(worldInfo: WorldInfo): MutableList<Biome> {
                return mutableListOf(Biome.PLAINS)
            }
        }
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        if (false) {
            val j = m.launchMainThread {
                val hd = DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(
                    m,
                    Location(Bukkit.getWorld("RevampedTutorialIsland"), -70.5, 109.45, -62.05, 180f, 0f)
                )

                val displayBlock = hd.addDisplayBlock()

                displayBlock.backgroundColor = Color.fromARGB(0, 0, 0, 0)
                displayBlock.billboard = Display.Billboard.FIXED
                displayBlock.transformation = Transformation(
                    displayBlock.transformation.translation,
                    displayBlock.transformation.leftRotation,
                    org.joml.Vector3f(
                        1.3f,
                        1.3f,
                        1.3f
                    ),
                    displayBlock.transformation.rightRotation,
                )

                val lines = createMarqueeSequence("Próxima Estação: Paraíso. Desembarque pelo lado direito. >>>>>>>>>>".uppercase(), 23)

                for (line in lines) {
                    println(line)
                    displayBlock.text(textComponent {
                        color(NamedTextColor.GOLD)
                        content(line)
                        font(Key.key("sparklypower", "doto"))
                    })
                    delayTicks(3L)
                }
            }
            return
        }


        if (false) {
            m.launchMainThread {
                player.sendMessage("Testing... 123")

                val x = measureTimedValue {
                    createWorld(
                        WorldCreator.name("RevampedTutorialIsland_LoadTest")
                            .biomeProvider(DreamEmptyWorldGenerator.EmptyBiomeProvider())
                            .generator(DreamEmptyWorldGenerator.EmptyWorldGenerator())
                            .type(WorldType.FLAT)
                            .keepSpawnLoaded(TriState.FALSE)
                            .generateStructures(false)
                    )!!
                }
                player.teleport(x.value.spawnLocation)
                Bukkit.broadcastMessage("Took $x! (load)")
                delayTicks(100L)
                player.teleport(
                    Location(Bukkit.getWorld("RevampedTutorialIsland"), -48.62368724611628, 106.0, -60.240612771078844, 127.28456f, 0.68098605f)
                )
                val y = measureTimedValue {
                    Bukkit.unloadWorld(x.value, false)
                }
                Bukkit.broadcastMessage("Took $y! (unload)")
                player.sendMessage("Done!")
            }
            return
        }

        if (false) {
            val j = m.launchMainThread {
                val location = Location(Bukkit.getWorld("RevampedTutorialIsland"), -78.5, 109.1, -62.05, 180f, 0f)

                location.world.getChunkAt(0, 0).getChunkSnapshot()
                val display = location.world.spawn(
                    location,
                    TextDisplay::class.java
                ) {
                    it.backgroundColor = Color.fromARGB(0, 0, 0, 0)
                    it.billboard = Display.Billboard.FIXED
                    it.transformation = Transformation(
                        it.transformation.translation,
                        it.transformation.leftRotation,
                        org.joml.Vector3f(
                            1.3f,
                            1.3f,
                            1.3f
                        ),
                        it.transformation.rightRotation,
                    )

                    it.isSeeThrough = false
                    it.lineWidth = 999
                    it.isPersistent = false
                    it.brightness = Display.Brightness(15, 15)
                    it.text(textComponent {
                        color(NamedTextColor.GOLD)
                        content("Próxima Estação: Paraíso. Desembarque pelo lado direito. ➔➔➔➔➔")
                    })
                }

                interpolateTeleport(
                    m,
                    display,
                    Location(Bukkit.getWorld("RevampedTutorialIsland"), -63.0, 109.1, -62.05, 180f, 0f),
                    200
                )
            }
            return
        }

        if (false) {
            val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

            val originalCamera = Location(Bukkit.getWorld("RevampedTutorialIsland"), -70.81624654641408, 104.50725588180369, -81.66072194890836, -151.55869f, -10.599642f)
            val targetCamera = Location(Bukkit.getWorld("RevampedTutorialIsland"), -69.08631353947341, 105.8242607754509, -85.02858059159348, -151.55869f, -10.599642f)

            val camera = revampedTutorialIslandWorld.spawnEntity(
                originalCamera,
                EntityType.TEXT_DISPLAY
            ) as TextDisplay

            camera.text(
                textComponent {
                    content("hewwo")
                }
            )

            player.gameMode = GameMode.SPECTATOR
            player.spectatorTarget = camera

            camera.billboard = Display.Billboard.CENTER

            camera.teleportDuration = 59

            m.launchMainThread {
                delayTicks(5L)
                camera.teleport(targetCamera)
            }

            return
        }

        if (false) {
            val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

            val npc = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
                m,
                Location(Bukkit.getWorld("RevampedTutorialIsland"), -68.39515605857557, 106.5, -85.89218845900189, 0.0f, 0f),
                player.name,
                null
            )

            val husk = npc.getEntity()!!

            val armorStandChair = revampedTutorialIslandWorld.spawnEntity(
                Location(Bukkit.getWorld("RevampedTutorialIsland"), -68.44959436117267, 104.5, -86.16593423674239, 1.5743821f, 5.329576f),
                EntityType.ARMOR_STAND
            )
            armorStandChair.addPassenger(husk)
            return
        }

        if (false) {
            val hd = DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(
                m,
                Location(Bukkit.getWorld("RevampedTutorialIsland"), -70.5, 109.45, -62.05, 180f, 0f)
            )

            val displayBlock = hd.addDisplayBlock()
            displayBlock.backgroundColor = Color.fromARGB(0, 0, 0, 0)
            displayBlock.billboard = Display.Billboard.FIXED
            displayBlock.transformation = Transformation(
                displayBlock.transformation.translation,
                displayBlock.transformation.leftRotation,
                org.joml.Vector3f(
                    1.3f,
                    1.3f,
                    1.3f
                ),
                displayBlock.transformation.rightRotation,
            )

            m.launchMainThread {
                val lines = createMarqueeSequence("Próxima Estação: Paraíso. Desembarque pelo lado direito. >>>>>>>>>>>>>>>>>>>>".uppercase(), 23)

                for (line in lines) {
                    println(line)
                    displayBlock.text(textComponent {
                        color(NamedTextColor.GOLD)
                        content(line)
                    })
                    delayTicks(2L)
                }
            }

            return
        }

        if (false) {
            val display = Bukkit.getWorld("RevampedTutorialIsland")!!.spawn(
                Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.0, 90.0, -99.69, 0f, 0f),
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
                    content("\uE293\uE294\uE295\uE296")
                })
            }
            return
        }

        m.launchAsyncThread {
            val skin = DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesBySparklyPowerUniqueId(player.uniqueId)
            val lorittaSkin = DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesByMojangName("Loritta")!!

            if (false) {
                player.sendMessage("done (maybe)")
                onMainThread {
                    if (false) {
                        player.gameMode = GameMode.SPECTATOR

                        val cutscene = SparklyCutscene(player)
                        cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.05606390654687, 87.75733943336236, -99.57196761075429, 179.92656f, 0.14808586f))

                        cutscene.easeCamera(
                            m,
                            Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.05606390654687, 87.75733943336236, -99.57196761075429, 179.92656f, 0.14808586f),
                            Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.05561667457742, 87.75733943336236, -95.88862066850649, 179.92656f, 0.14808586f),
                            90
                        ).join()

                        delayTicks(10L)

                        val npc2 = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
                            m,
                            Location(
                                Bukkit.getWorld("RevampedTutorialIsland"),
                                -75.02086775074017,
                                87.0,
                                -99.67983456303703,
                                0.21942139f,
                                2.9611778f
                            ),
                            "Loritta",
                            lorittaSkin.let { SkinTexture(it.value, it.signature!!) }
                        )

                        player.playSound(player, "sparklypower:general.discord_message", 1f, 1f)

                        player.spawnParticle(Particle.HAPPY_VILLAGER, npc2.location, 25, 0.25, 0.25, 0.25)
                        repeat(10) {
                            npc2.teleport(npc2.location.add(0.0, -0.2, 0.0))
                            delayTicks(1L)
                        }
                        cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -76.74684348754286, 85.81913897026025, -94.47603241905854, -146.97168f, 0.94750345f))
                        delayTicks(100L)
                        cutscene.end()
                        return@onMainThread
                    }

                    val cutscene = SparklyCutscene(player)
                    player.gameMode = GameMode.SPECTATOR
                    cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -74.66008065673269, 85.33780804352767, -99.31625717726567, 146.411f, -4.2042894f))
                    delayTicks(40L)

                    cutscene.easeCamera(
                        m,
                        listOf(
                            InterpolationKeyframe(
                                Location(Bukkit.getWorld("RevampedTutorialIsland"), -74.73470102060656, 85.31115016843945, -99.1509472409148, 151.80084f, -3.0495923f),
                                null
                            ),
                            InterpolationKeyframe(
                                Location(Bukkit.getWorld("RevampedTutorialIsland"), -74.73470102060656, 86.85755729105435, -99.1509472409148, 151.1792f, 7.6092563f),
                                90
                            )
                        )
                    ).join()
                    cutscene.end()
                }
                return@launchAsyncThread
            }

            val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

            onMainThread {
                val cutscene = SparklyCutscene(player)
                cutscene.start()
                val npc = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
                    m,
                    Location(Bukkit.getWorld("RevampedTutorialIsland"), -68.39515605857557, 106.5, -85.89218845900189, 0.0f, 0f),
                    player.name,
                    skin?.let { SkinTexture(it.value, it.signature!!) }
                )

                val sparklyCutscene = SparklyTutorialCutscene(m, player, cutscene, npc)

                if (false) {
                    sparklyCutscene.spawnSittingNPC()
                    cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -69.2227185770231, 107.77459719593291, -86.27805483484802, -30.466305f, 55f))
                    return@onMainThread
                }

                if (false) {
                    val display = sparklyCutscene.createLorittaMessage()
                    val npc = sparklyCutscene.spawnLorittaMessageSelfNPC()
                    m.launchMainThread {
                        delayTicks(200L)
                        display.remove()
                        npc.remove()
                    }
                    return@onMainThread
                }

                player.teleport(Location(Bukkit.getWorld("RevampedTutorialIsland"), -68.5, 106.0, -85.5, 0.0f, 0.0f))

                if (false) {
                    sparklyCutscene.focusNextStationHologram()
                    sparklyCutscene.easeFromNextStationHologramToBench().join()
                    sparklyCutscene.endCutscene()
                    return@onMainThread
                }

                player.playSound(player, "sparklypower:general.metro_trecho_sp", 1f, 1f)

                val originalCamera = Location(Bukkit.getWorld("RevampedTutorialIsland"), -70.59881234126554, 106.6214850545078, -62.31686249472241, -179.51624f, -11.665535f)
                val targetCamera = Location(Bukkit.getWorld("RevampedTutorialIsland"), -70.58570933902557, 106.6214850545078, -80.4733926209905, -179.57544f, -10.866125f)
                val targetCamera2 = Location(Bukkit.getWorld("RevampedTutorialIsland"), -69.03694868977868, 107.4, -84.7289952581889, -152.9267f, 0.91783327f)

                cutscene.setCameraLocation(originalCamera)
                val cameraMove = m.launchMainThread {
                    cutscene.easeCamera(
                        m,
                        listOf(
                            InterpolationKeyframe(
                                originalCamera,
                                null
                            ),
                            InterpolationKeyframe(
                                targetCamera,
                                120
                            ),
                            InterpolationKeyframe(
                                targetCamera2,
                                40
                            )
                        )
                    )
                }

                /* val npc2 = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
                    m,
                    Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.02086775074017, 85.0, -99.67983456303703, 0.21942139f, 2.9611778f),
                    "Loritta",
                    lorittaSkin.let { SkinTexture(it.value, it.signature!!) }
                ) */
                sparklyCutscene.spawnSittingNPC()
                /* val husk = npc.getEntity()!!

                val armorStandChair = revampedTutorialIslandWorld.spawnEntity(
                    Location(Bukkit.getWorld("RevampedTutorialIsland"), -68.44959436117267, 104.5, -86.16593423674239, 1.5743821f, 5.329576f),
                    EntityType.ARMOR_STAND
                )
                armorStandChair.addPassenger(husk) */

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

                player.sendMessage(
                    textComponent {
                        appendTextComponent {
                            color(NamedTextColor.DARK_PURPLE)
                            decorate(TextDecoration.BOLD)
                            content("Narrador")
                        }
                        appendTextComponent {
                            color(NamedTextColor.GOLD)
                            content(" ➤ ")
                        }
                        appendTextComponent {
                            content("Bem-vindo...")
                        }
                    }
                )

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

                player.sendMessage(
                    textComponent {
                        appendTextComponent {
                            color(NamedTextColor.DARK_PURPLE)
                            decorate(TextDecoration.BOLD)
                            content("Narrador")
                        }
                        appendTextComponent {
                            color(NamedTextColor.GOLD)
                            content(" ➤ ")
                        }
                        appendTextComponent {
                            content("Este é você, iniciando uma nova jornada")
                        }
                    }
                )

                player.sendMessage(
                    textComponent {
                        appendTextComponent {
                            color(NamedTextColor.DARK_PURPLE)
                            decorate(TextDecoration.BOLD)
                            content("Narrador")
                        }
                        appendTextComponent {
                            color(NamedTextColor.GOLD)
                            content(" ➤ ")
                        }
                        appendTextComponent {
                            content("Tudo por causa daquela garota...")
                        }
                    }
                )
                /* player.showTitle(
                    Title.title(
                        textComponent {},
                        textComponent {
                            content("Este é você...")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(20), Ticks.duration(10))
                    )
                ) */

                delayTicks(100L)
                cameraMove.join()
                delay(40L)
                cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -69.2227185770231, 107.77459719593291, -86.27805483484802, -30.466305f, 55f))
                player.showTitle(
                    Title.title(
                        textComponent {},
                        textComponent {
                            content("Por causa da mensagem daquela garota...")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                    )
                )

                delayTicks(80L)

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

                // delayTicks(100L)

                /* player.showTitle(
                    Title.title(
                        textComponent {},
                        textComponent {
                            content("Aquela garota realmente sabe convencer as pessoas")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(20), Ticks.duration(10))
                    )
                ) */

                delayTicks(5L)
                // cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.02689541411704, 86.8, -94.30000001192093, -179.41255f, -0.44373846f))

                run {
                    player.gameMode = GameMode.SPECTATOR

                    val cutscene = SparklyCutscene(player)
                    cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.0, 89.75, -99.5, 180.0f, 0.0f))

                    val display = sparklyCutscene.createLorittaMessage()

                    val npc = sparklyCutscene.spawnLorittaMessageSelfNPC()

                    cutscene.easeCamera(
                        m,
                        Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.0, 89.75, -99.5, 180.0f, 0.0f),
                        Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.0, 89.75, -95.5, 180.0f, 0.0f),
                        90
                    ).join()

                    delayTicks(10L)

                    val npc2 = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
                        m,
                        Location(
                            Bukkit.getWorld("RevampedTutorialIsland"),
                            -75.02086775074017,
                            87.0,
                            -99.67983456303703,
                            0.21942139f,
                            2.9611778f
                        ),
                        "Loritta",
                        lorittaSkin.let { SkinTexture(it.value, it.signature!!) }
                    )

                    player.playSound(player, "sparklypower:general.discord_message", 1f, 1f)

                    player.spawnParticle(Particle.HAPPY_VILLAGER, npc2.location, 25, 0.25, 0.25, 0.25)
                    repeat(10) {
                        npc2.teleport(npc2.location.add(0.0, -0.2, 0.0))
                        delayTicks(1L)
                    }
                    cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -76.71498220751405, 86.66183149803553, -96.25794072057725, -158.19145f, -3.6714854f))
                    // cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -76.74684348754286, 85.81913897026025, -94.47603241905854, -146.97168f, 0.94750345f))
                    delayTicks(100L)
                    // cutscene.end()
                    npc.remove()
                    npc2.remove()
                    display.remove()
                }

                // delayTicks(100L)

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

                val job = sparklyCutscene.playbackLeavingHouseCutscene()
                cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -75.0, 77.8, -104.5, -90f, 0f))

                delayTicks(20L)

                player.showTitle(
                    Title.title(
                        textComponent {},
                        textComponent {
                            content("Onde até quem começa do zero...")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                    )
                )

                delayTicks(100L)

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

                cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -76.25097529609799, 86.75533307521188, -72.8356438421399, -134.244f, 7.7575636f))

                val playback = sparklyCutscene.playbackMiningCutscene()

                delayTicks(20L)

                player.showTitle(
                    Title.title(
                        textComponent {},
                        textComponent {
                            content("Se esforça e etc...")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                    )
                )

                delayTicks(100L)
                playback.join()

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

                sparklyCutscene.focusNextStationHologram()
                val nextStationJob = sparklyCutscene.playNextStationHologram()

                player.sendMessage(
                    textComponent {
                        appendTextComponent {
                            color(NamedTextColor.DARK_PURPLE)
                            decorate(TextDecoration.BOLD)
                            content("Narrador")
                        }
                        appendTextComponent {
                            color(NamedTextColor.GOLD)
                            content(" ➤ ")
                        }
                        appendTextComponent {
                            content("As possibilidades são infinitas... A sua história começa agora.")
                        }
                    }
                )
                nextStationJob.join()

                delayTicks(20L)

                sparklyCutscene.easeFromNextStationHologramToBench().join()
                delayTicks(60L)
                sparklyCutscene.endCutscene()

                // OPEN THE F*CKING DOOR!!!
                val area1 = Location(Bukkit.getWorld("RevampedTutorialIsland"), -74.61293046088464, 106.5, -62.259528339462506, 176.55438f, 10.066665f)
                val area2 = Location(Bukkit.getWorld("RevampedTutorialIsland"), -74.6283791455245, 106.5, -86.39881375064365, -179.62628f, 5.5662723f)

                BlockUtils.getBlocksFromTwoLocations(area1, area2).forEach {
                    if (it.type == Material.IRON_DOOR) {
                        val blockData = it.blockData as Door
                        blockData.isOpen = true
                        it.setBlockData(blockData)
                    }
                }

                npc.remove()
                // npc2.remove()
                // armorStandChair.remove()
                // displayBlock.remove()

                cutscene.end()

                delayTicks(100L)

                BlockUtils.getBlocksFromTwoLocations(area1, area2).forEach {
                    if (it.type == Material.IRON_DOOR) {
                        val blockData = it.blockData as Door
                        blockData.isOpen = false
                        it.setBlockData(blockData)
                    }
                }
            }
        }
    }

    suspend fun sendTitleAndWait(
        player: Player,
        title: Title
    ) {
        player.showTitle(
            title
        )

        val timesPart = title.part(TitlePart.TIMES)
        val fadeIn = timesPart.fadeIn().toMillis() / Ticks.SINGLE_TICK_DURATION_MS
        val stay = timesPart.stay().toMillis() / Ticks.SINGLE_TICK_DURATION_MS
        val fadeOut = timesPart.fadeOut().toMillis() / Ticks.SINGLE_TICK_DURATION_MS
        delayTicks(fadeIn + stay + fadeOut)
    }
}