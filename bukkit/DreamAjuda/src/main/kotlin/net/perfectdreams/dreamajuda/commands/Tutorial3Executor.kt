package net.perfectdreams.dreamajuda.commands

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import net.kyori.adventure.util.TriState
import net.minecraft.core.Holder
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.*
import net.perfectdreams.dreamajuda.*
import net.perfectdreams.dreamajuda.cutscenes.CutsceneEntityManager
import net.perfectdreams.dreamajuda.cutscenes.SparklyCutsceneCamera
import net.perfectdreams.dreamajuda.cutscenes.SparklyTutorialCutsceneConfig
import net.perfectdreams.dreamajuda.cutscenes.SparklyTutorialCutsceneFinalCut
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.BlockUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.sendPacket
import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamemptyworldgenerator.EmptyBiomeProvider
import net.perfectdreams.dreamemptyworldgenerator.EmptyWorldGenerator
import org.bukkit.*
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*
import kotlin.time.measureTimedValue

class Tutorial3Executor(val m: DreamAjuda) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val section = word("section") { context, builder ->
            CutsceneSection.entries.forEach {
                builder.suggest(it.name.lowercase())
            }
        }
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val section = CutsceneSection.valueOf(args[options.section].uppercase())

        m.launchAsyncThread {
            val skin = DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesBySparklyPowerUniqueId(player.uniqueId)
            val lorittaSkin = DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesByMojangName("Loritta")!!

            val config = File(m.dataFolder, "cutscene_tutorial.yml")
                .readText()
                .let {
                    Yaml.default.decodeFromString<SparklyTutorialCutsceneConfig>(it)
                }

            onMainThread {
                val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

                // This is the end position of the cutscene, we also NEED to teleport the player to there to cause the chunks to load
                player.teleport(config.playerCutsceneFinishLocation.toLocation(revampedTutorialIslandWorld))

                // We need to delay it by one tick to let the chunks to ACTUALLY be loaded, to avoid NPE when attempting to create the GlobalSceneObjects
                // We delay it for 20 ticks to avoid any laggy clients causing issues
                delayTicks(20L)

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
                val cutscene = when (section) {
                    CutsceneSection.ALL -> SparklyTutorialCutsceneFinalCut(m, player, cutsceneCamera, revampedTutorialIslandWorld, config, entityManager, gso, skin, lorittaSkin)
                    CutsceneSection.THE_BEGINNING -> SparklyTutorialCutsceneFinalCut.TheBeginning(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin)
                    CutsceneSection.BORING_OFFICE -> SparklyTutorialCutsceneFinalCut.BoringOffice(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin)
                    CutsceneSection.LOOK_AT_PHONE -> SparklyTutorialCutsceneFinalCut.LookAtPhone(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin)
                    CutsceneSection.LORITTA_MESSAGE -> SparklyTutorialCutsceneFinalCut.LorittaMessage(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin, lorittaSkin)
                    CutsceneSection.HOME_SWEET_HOME -> SparklyTutorialCutsceneFinalCut.HomeSweetHome(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin)
                    CutsceneSection.MINING_STUFFZ -> SparklyTutorialCutsceneFinalCut.MiningStuffz(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin)
                    CutsceneSection.LEAVE_YOUR_MARK -> SparklyTutorialCutsceneFinalCut.LeaveYourMark(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin)
                    CutsceneSection.LIFETIME_FRIENDS -> SparklyTutorialCutsceneFinalCut.LifetimeFriends(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin)
                    CutsceneSection.NEXT_STATION -> SparklyTutorialCutsceneFinalCut.NextStationFocus(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin)
                    CutsceneSection.BLACK_WALL_FADE -> SparklyTutorialCutsceneFinalCut.BlackWallFade(m, player, cutsceneCamera, entityManager, revampedTutorialIslandWorld, config, gso, skin)
                }
                cutscene.start()
                cutscene.end(true)
                gso.remove()
            }
        }
    }

    enum class CutsceneSection {
        ALL,
        THE_BEGINNING,
        BORING_OFFICE,
        LOOK_AT_PHONE,
        LORITTA_MESSAGE,
        HOME_SWEET_HOME,
        MINING_STUFFZ,
        LIFETIME_FRIENDS,
        LEAVE_YOUR_MARK,
        NEXT_STATION,
        BLACK_WALL_FADE
    }
}