package net.perfectdreams.dreamajuda.commands

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import net.kyori.adventure.util.TriState
import net.perfectdreams.dreamajuda.tutorials.SparklyTutorial
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.cutscenes.CutsceneEntityManager
import net.perfectdreams.dreamajuda.cutscenes.SparklyCutsceneCamera
import net.perfectdreams.dreamajuda.cutscenes.SparklyTutorialCutsceneConfig
import net.perfectdreams.dreamajuda.cutscenes.SparklyTutorialCutsceneFinalCut
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffectsAwait
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamemptyworldgenerator.EmptyBiomeProvider
import net.perfectdreams.dreamemptyworldgenerator.EmptyWorldGenerator
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import java.io.File
import kotlin.time.measureTimedValue

class CutsceneExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
    companion object {
        // TODO: Do everything on the same world, or do everything in different worlds?
        //  My concern is that creating a different world for each player will lag the server, because loading worlds is a bit expensive
        //  Took 13.922030ms to load the ephemeral world for jubbskkk
        //  Took 3.222899ms to unload the ephemeral world for jubbskkk
        //  It isn't that expensive, but you also need to think how to handle WHEN the world will be loaded/unloaded

        const val COPY_WORLD_FROM_TEMPLATE_WORLD = false
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        m.launchAsyncThread {
            val skin = DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesBySparklyPowerUniqueId(player.uniqueId)
            val lorittaSkin = DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesByMojangName("Loritta")!!

            val config = File(m.dataFolder, "cutscene_tutorial.yml")
                .readText()
                .let {
                    Yaml.default.decodeFromString<SparklyTutorialCutsceneConfig>(it)
                }

            if (!COPY_WORLD_FROM_TEMPLATE_WORLD) {
                onMainThread {
                    val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

                    m.startTutorial(player, SparklyTutorial.LeaveTheSubway::class)

                    // Bukkit.broadcastMessage("Took ${x.duration} to load the world!")

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

                    // We don't need to take care that we aren't hiding ourselves because the server already checks it for us
                    // We use hideEntity instead of hidePlayer because we don't want to remove the player from the TAB list
                    revampedTutorialIslandWorld.players
                        .forEach {
                            // Hide all players on this world from that player
                            it.hideEntity(m, player)

                            // And hide everyone to ourselves
                            player.hideEntity(m, it)
                        }

                    // We need to delay it by one tick to let the chunks to ACTUALLY be loaded, to avoid NPE when attempting to create the GlobalSceneObjects
                    delayTicks(1L)

                    val entityManager = CutsceneEntityManager(m, player)
                    val gso = SparklyTutorialCutsceneFinalCut.GlobalSceneObjects(
                        entityManager,
                        player,
                        m,
                        player.world,
                        config,
                        skin
                    )

                    val cutsceneCamera = SparklyCutsceneCamera(m, player)
                    val cutscene = SparklyTutorialCutsceneFinalCut(
                        m,
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

                    // player.teleportToServerSpawnWithEffectsAwait()

                    revampedTutorialIslandWorld.players
                        .forEach {
                            // And now we revert
                            it.showEntity(m, player)
                            player.showEntity(m, it)
                        }
                }
            } else if (COPY_WORLD_FROM_TEMPLATE_WORLD) {
                // TODO: Add a mutex or, if the user is in a revamped tutorial world, kick them out
                // Alt version that loads the template world!
                val worldName = "EphemeralWorld_RevampedTutorialWorld_${player.name}"
                File(m.dataFolder, "template_world").copyRecursively(File(worldName), true)

                onMainThread {
                    val x = measureTimedValue {
                        Bukkit.createWorld(
                            WorldCreator.name(worldName)
                                .biomeProvider(EmptyBiomeProvider())
                                .generator(EmptyWorldGenerator())
                                .type(WorldType.FLAT)
                                .keepSpawnLoaded(TriState.FALSE)
                                .generateStructures(false)
                        )!!
                    }

                    m.logger.info("Took ${x.duration} to load the ephemeral world for ${player.name}")

                    val revampedTutorialIslandWorld = x.value
                    revampedTutorialIslandWorld.isAutoSave = false

                    // Bukkit.broadcastMessage("Took ${x.duration} to load the world!")

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

                    // We need to delay it by one tick to let the chunks to ACTUALLY be loaded, to avoid NPE when attempting to create the GlobalSceneObjects
                    delayTicks(1L)

                    val entityManager = CutsceneEntityManager(m, player)
                    val gso = SparklyTutorialCutsceneFinalCut.GlobalSceneObjects(
                        entityManager,
                        player,
                        m,
                        player.world,
                        config,
                        skin
                    )

                    val cutsceneCamera = SparklyCutsceneCamera(m, player)
                    val cutscene = SparklyTutorialCutsceneFinalCut(
                        m,
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
                    cutscene.cleanUp()
                    gso.remove()

                    player.teleportToServerSpawnWithEffectsAwait()

                    // We need to delay by 1 tick to avoid issues
                    delayTicks(1L)

                    val y = measureTimedValue {
                        Bukkit.unloadWorld(worldName, false)
                    }

                    m.logger.info("Took ${y.duration} to unload the ephemeral world for ${player.name}")
                    // Bukkit.broadcastMessage("Took ${y.duration} to unload the world!")

                    /* onAsyncThread {
                    File(worldName).deleteRecursively()
                } */
                }
            }
        }
    }
}