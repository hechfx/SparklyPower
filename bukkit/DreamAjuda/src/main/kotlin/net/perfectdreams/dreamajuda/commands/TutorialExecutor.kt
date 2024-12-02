package net.perfectdreams.dreamajuda.commands

import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.kyori.adventure.util.Ticks
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundSwingPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Pose
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.RecordedAnimation
import net.perfectdreams.dreamajuda.SparklyCutscene
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.npc.SparklyNPCManager
import net.perfectdreams.dreamcore.utils.npc.user.UserCreatedNPCData
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreamcore.utils.skins.SkinUtils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.Sign
import org.bukkit.block.data.type.Door
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.EntityType
import org.bukkit.entity.Husk
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File

class TutorialExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        if (false) {
            val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!
            val npc = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
                m,
                Location(revampedTutorialIslandWorld, 4.5, 106.9, 16.5, 270.0f, 0.0f),
                player.name,
                null
            )
            val husk = (npc.getEntity()!! as Husk)
            Bukkit.getMobGoals().removeAllGoals(husk)
            husk.setAI(true)
            Bukkit.broadcastMessage("${husk.getAttribute(Attribute.MOVEMENT_SPEED)!!.baseValue}")
            husk.getAttribute(Attribute.MOVEMENT_SPEED)!!.baseValue = 0.3
            // husk.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 0, true))

            m.launchMainThread {
                delayTicks(20L)
                val pathfinding = husk.pathfinder.moveTo(
                    Location(revampedTutorialIslandWorld, 4.5, 106.9, 29.5, 270.0f, 0.0f)
                )

                Bukkit.broadcastMessage("Pathfinder Result: $pathfinding")
            }

            m.launchMainThread {
                delayTicks(500L)
                npc.remove()
            }
            return
        }

        m.launchAsyncThread {
            val skin = DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesBySparklyPowerUniqueId(player.uniqueId)
            val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

            onMainThread {
                val cutscene = SparklyCutscene(player)
                cutscene.start()

                val doorBlock = revampedTutorialIslandWorld.getBlockAt(Location(revampedTutorialIslandWorld, 2.512499988079071, 107.0, 16.471243083556118, -87.762375f, -1.0066427f))
                val doorBlockData = doorBlock.blockData as Door
                doorBlockData.isOpen = false
                doorBlock.blockData = doorBlockData

                val npc = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
                    m,
                    Location(revampedTutorialIslandWorld, -0.5, 107.0, 16.5, 270.0f, 0.0f),
                    player.name,
                    skin?.let { SkinTexture(it.value, it.signature!!) }
                )

                player.playSound(doorBlock.location, "minecraft:music_disc.far", 1000f, 1f)

                val husk = (npc.getEntity()!! as Husk)
                // husk.setAI(true)
                // Bukkit.getMobGoals().removeAllGoals(husk)
                // Bukkit.broadcastMessage("${husk.getAttribute(Attribute.MOVEMENT_SPEED)!!.baseValue}")
                // husk.getAttribute(Attribute.MOVEMENT_SPEED)!!.baseValue = 0.3

                cutscene.setCameraLocation(Location(revampedTutorialIslandWorld, 6.5, 105.9, 16.5, 90.0f, 0.0f))

                player.showTitle(
                    Title.title(
                        textComponent {
                            color(NamedTextColor.BLACK)
                            content("\uE292")
                        },
                        textComponent {},
                        Title.Times.times(Ticks.duration(0), Ticks.duration(10), Ticks.duration(10))
                    )
                )

                delayTicks(10L)

                /* val pf = husk.pathfinder.findPath(Location(revampedTutorialIslandWorld, 4.5, 105.9, 16.5, 270.0f, 0.0f))
                pf.points.forEach {
                    println(it)
                } */

                // husk.pathfinder.moveTo(Location(revampedTutorialIslandWorld, 1.5044497918227682, 107.0, 16.377521885729852, -89.69144f, 90.0f))

                val playback = m.keyframerManager.playbackRecording(
                    player,
                    npc,
                    Json.decodeFromString<RecordedAnimation>(
                        File(m.dataFolder, "intro.sparklyanim").readText()
                    )
                ) {
                    if (it == 14) {
                        player.playSound(player, "minecraft:block.wooden_door.open", 1f, 1f)
                        doorBlockData.isOpen = true
                        doorBlock.blockData = doorBlockData
                    }

                    if (it == 33) {
                        player.playSound(player, "minecraft:block.wooden_door.close", 1f, 1f)
                        doorBlockData.isOpen = false
                        doorBlock.blockData = doorBlockData
                    }
                }

                /* m.launchMainThread {
                    delayTicks(10L)
                    val packet = ClientboundAnimatePacket(
                        (npc.getEntity()!! as CraftEntity).handle,
                        ClientboundAnimatePacket.SWING_MAIN_HAND
                    )

                    (player as CraftPlayer).handle.connection.sendPacket(packet)

                    doorBlockData.isOpen = true
                    doorBlock.blockData = doorBlockData

                    player.playSound(doorBlock.location, "minecraft:block.wooden_door.open", 1f, 1f)

                    delayTicks(5L)
                    val b = husk.pathfinder.moveTo(Location(revampedTutorialIslandWorld, 4.5, 105.9, 16.5, 270.0f, 0.0f))
                    println("path: $b")
                } */

                delayTicks(10L)

                // Bukkit.broadcastMessage("Pathfinder Result: $pf")

                player.showTitle(
                    Title.title(
                        textComponent {

                        },
                        textComponent {
                            content("A sua vida é normal...")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                    )
                )

                repeat(70) {
                    cutscene.moveCamera(cutscene.cameraLocation.add(0.02, 0.0, 0.0))
                    delayTicks(1L)
                }

                // husk.pathfinder.stopPathfinding()

                playback.join()

                cutscene.setCameraLocation(Location(revampedTutorialIslandWorld, 33.5877661311996, 105.9375, 29.048546873450274, -115.18036f, 2.5758905f))
                npc.teleport(Location(revampedTutorialIslandWorld, 39.520113620755, 106.0, 27.79640947353085, 137.96458f, 0.53294504f))

                player.showTitle(
                    Title.title(
                        textComponent {

                        },
                        textComponent {
                            content("Familiar...")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                    )
                )

                val block = Location(Bukkit.getWorld("RevampedTutorialIsland"), 38.52377075971833, 107.0, 26.46427409285512, -89.86536f, 7.52047f)

                /* val droppedItem = m.launchMainThreadDeferred {
                    repeat(8) {
                        Bukkit.broadcastMessage("swing!")
                        val packet = ClientboundAnimatePacket(
                            (npc.getEntity()!! as CraftEntity).handle,
                            ClientboundAnimatePacket.SWING_MAIN_HAND
                        )

                        (player as CraftPlayer).handle.connection.sendPacket(packet)
                        player.sendBlockDamage(block, (it + 1) * 0.12f)
                        player.playSound(block, "minecraft:block.wood.hit", 1f, 0.5f)
                        delayTicks(5L)
                    }
                    player.playSound(block, "minecraft:block.wood.break", 1f, 1f)
                    player.sendBlockChange(block, Bukkit.createBlockData(Material.AIR))

                    revampedTutorialIslandWorld.dropItemNaturally(
                        block,
                        ItemStack.of(Material.OAK_WOOD)
                    )
                } */

                val playback2 = m.keyframerManager.playbackRecording(
                    player,
                    npc,
                    Json.decodeFromString<RecordedAnimation>(
                        File(m.dataFolder, "test.sparklyanim").readText()
                    )
                ) {
                    /* if (it == 14) {
                        player.playSound(player, "minecraft:block.wooden_door.open", 1f, 1f)
                        doorBlockData.isOpen = true
                        doorBlock.blockData = doorBlockData
                    }

                    if (it == 33) {
                        player.playSound(player, "minecraft:block.wooden_door.close", 1f, 1f)
                        doorBlockData.isOpen = false
                        doorBlock.blockData = doorBlockData
                    } */
                }

                delayTicks(70L)
                playback2.join()
                player.sendBlockChange(block, block.block.blockData)
                // droppedItem.await().remove()

                val armorStandChair = revampedTutorialIslandWorld.spawnEntity(
                    Location(Bukkit.getWorld("RevampedTutorialIsland"), 19.960018480008713, 104.5, -11.44828222791071, 89.4079f, 22.798521f),
                    EntityType.ARMOR_STAND
                )
                armorStandChair.addPassenger(husk)

                cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), 23.340702313722034, 105.24264481781874, -12.691633625192843, 74.722626f, -0.59172475f))

                player.showTitle(
                    Title.title(
                        textComponent {

                        },
                        textComponent {
                            content("Confortável...")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                    )
                )

                delayTicks(70L)
                cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), 17.864180691891292, 105.23710151852015, -10.51611608526244, -108.7305f, -8.082534f))

                player.showTitle(
                    Title.title(
                        textComponent {

                        },
                        textComponent {
                            content("Mas...")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(20), Ticks.duration(10))
                    )
                )

                delayTicks(40L)

                player.showTitle(
                    Title.title(
                        textComponent {

                        },
                        textComponent {
                            content("Alguma coisa está faltando...")
                        },
                        Title.Times.times(Ticks.duration(10), Ticks.duration(40), Ticks.duration(10))
                    )
                )

                npc.teleport(Location(Bukkit.getWorld("RevampedTutorialIsland"), 0.5, 107.5625, 17.5, 0f, 0f))
                cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), 1.6725903860507947, 108.45272099590953, 16.380459609659084, 55.627064f, 42.90177f))

                npc.setPose(org.bukkit.entity.Pose.SLEEPING)

                delayTicks(70L)

                player.showTitle(
                    Title.title(
                        textComponent {
                            color(NamedTextColor.WHITE)
                            content("\uE292")
                        },
                        textComponent {},
                        Title.Times.times(Ticks.duration(10), Ticks.duration(10), Ticks.duration(10))
                    )
                )

                delayTicks(10L)

                cutscene.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), 69.41036902319605, 106.0, 10.305569364831834, -179.74754f, -1.5099363f))

                val j = m.launchMainThread {
                    repeat(20) {
                        player.playSound(player, "minecraft:entity.minecart.riding", 1f, 1f)
                        delayTicks(20L)
                    }
                }

                delayTicks(100L)
                j.cancel()

                player.playSound(player, "minecraft:block.piston.contract", 1f, 0.7f)
                doorBlockData.isOpen = true
                doorBlock.blockData = doorBlockData

                player.stopSound("minecraft:music_disc.far")
                armorStandChair.remove()
                npc.remove()

                player.gameMode = GameMode.SURVIVAL

                cutscene.end()
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