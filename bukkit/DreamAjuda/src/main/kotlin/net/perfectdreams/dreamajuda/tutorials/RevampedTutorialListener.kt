package net.perfectdreams.dreamajuda.tutorials

import com.griefprevention.visualization.BoundaryVisualization
import com.griefprevention.visualization.VisualizationType
import me.ryanhamshire.GriefPrevention.util.BoundingBox
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.network.protocol.game.*
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamchat.events.PlayerReceivePlayerChatEvent
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamcore.utils.get
import net.perfectdreams.dreamcore.utils.packetevents.ClientboundPacketSendEvent
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamkits.events.PlayerKitReceiveEvent
import net.perfectdreams.dreamscoreboard.events.PlayerScoreboardRefreshEvent
import net.perfectdreams.dreamwarps.events.PlayerWarpTeleportEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

class RevampedTutorialListener(val m: DreamAjuda) : Listener {
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    fun onKick(e: PlayerKickEvent) {
        // Do not kick players for flying during the tutorial
        val activeTutorial = m.activeTutorials[e.player]

        if (activeTutorial != null && e.cause == PlayerKickEvent.Cause.FLYING_PLAYER)
            e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    fun onDropItem(e: PlayerDropItemEvent) {
        val activeTutorial = m.activeTutorials[e.player]

        if (activeTutorial != null) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    fun onChat(e: PlayerReceivePlayerChatEvent) {
        val activeTutorial = m.activeTutorials[e.receiver]

        // Don't receive chat messages if we are in a tutorial
        if (activeTutorial != null) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    fun onChat(e: AsyncPlayerChatEvent) {
        val activeTutorial = m.activeTutorials[e.player]

        // Don't let the player send messages if they are in a tutorial
        if (activeTutorial != null) {
            e.isCancelled = true
            e.player.sendMessage(
                textComponent {
                    color(NamedTextColor.RED)
                    appendTextComponent {
                        content("Você não pode enviar mensagens no chat!")
                        appendNewline()
                    }
                    appendTextComponent {
                        appendTextComponent {
                            content("Se você quer pular o tutorial, use ")
                        }
                        appendCommand("/pulartutorial")
                        appendTextComponent {
                            content(", mas não venha chorar falando que você está perdido no servidor, ok?")
                        }
                    }
                }
            )
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val activeTutorial = m.activeTutorials[e.player]

        // End the tutorial if the player left during the tutorial
        if (activeTutorial != null) {
            m.endTutorial(e.player)
            e.player.teleportToServerSpawn()
        }
    }

    @EventHandler
    fun onTeleportCutscene(e: PlayerTeleportEvent) {
        if (e.to.world.name != m.tutorialConfig.worldName && e.from.world != e.to.world) {
            m.cutsceneJobs[e.player]?.cancel()
        }
    }

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        val activeTutorial = m.activeTutorials[e.player]

        if (activeTutorial != null && e.to.world.name != m.tutorialConfig.worldName && e.from.world != e.to.world ) {
            // If we have an active tutorial, and we have teleported away from the current world, end the current tutorial ASAP!
            m.endTutorial(e.player)
        }
    }

    @EventHandler
    fun onTeleport(e: EntityDamageEvent) {
        if (e.entity is Player && e.entity.world.name == m.tutorialConfig.worldName) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onFood(e: FoodLevelChangeEvent) {
        if (e.entity.world.name == m.tutorialConfig.worldName)
            e.isCancelled = true
    }

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (!e.hasExplicitlyChangedBlock())
            return

        val activeTutorial = m.activeTutorials[e.player]?.activeTutorial

        if (activeTutorial is SparklyTutorial.LeaveTheSubway && !activeTutorial.isCompleted) {
            if (e.player.location.isWithinRegion("revampedtutorial_outsidesubway")) {
                activeTutorial.playerTutorial.launchMainThreadTutorialTask {
                    activeTutorial.onComplete()
                }
            }
        }
    }

    @EventHandler
    fun onKitReceive(e: PlayerKitReceiveEvent) {
        val playerTutorial = m.activeTutorials[e.player]?.activeTutorial ?: return

        if (e.kit.name == "noob") {
            if (!playerTutorial.isCompleted) {
                playerTutorial.playerTutorial.launchMainThreadTutorialTask {
                    playerTutorial.onComplete()
                }
                return
            }
        } else {
            e.isCancelled = true
            e.player.sendMessage {
                textComponent {
                    color(NamedTextColor.RED)
                    content("Você não pode usar isto agora!")
                }
            }
        }
    }

    @EventHandler
    fun onWarp(e: PlayerWarpTeleportEvent) {
        val playerTutorial = m.activeTutorials[e.player]

        if (playerTutorial != null) {
            val activeTutorial = playerTutorial.activeTutorial

            if (activeTutorial is SparklyTutorial.WarpResources && !activeTutorial.isCompleted && e.warpName == "recursos") {
                e.warpTarget = Location(Bukkit.getWorld("RevampedTutorialIsland"), -119.50872005350459, 121.0, -32.57481782079832, 180f, 0f)

                activeTutorial.playerTutorial.launchMainThreadTutorialTask {
                    activeTutorial.onComplete()
                }
                return
            }

            if (activeTutorial is SparklyTutorial.WarpSurvival && !activeTutorial.isCompleted && e.warpName == "survival") {
                e.warpTarget = Location(Bukkit.getWorld("RevampedTutorialIsland"), -177.41760131046655, 126.0, 14.400560034621721, 0.05712184f, 2.5177238f)

                activeTutorial.playerTutorial.launchMainThreadTutorialTask {
                    activeTutorial.onComplete()
                }
                return
            }

            e.player.sendMessage {
                e.isCancelled = true
                textComponent {
                    color(NamedTextColor.RED)
                    content("Você não pode usar isto agora!")
                }
            }
            return
        }
    }

    @EventHandler
    fun onScoreboard(e: PlayerScoreboardRefreshEvent) {
        val activeTutorial = m.activeTutorials[e.player]?.activeTutorial ?: return

        e.block = {
            var usedLines = 15

            /* phoenix.setText(
                textComponent {
                    color(NamedTextColor.GOLD)
                    decorate(TextDecoration.BOLD)
                    content("O Prelúdio")
                }, usedLines--
            )

            phoenix.setText(textComponent {}, usedLines--) */

            phoenix.setText(
                textComponent {
                    color(NamedTextColor.YELLOW)
                    appendTextComponent {
                        decorate(TextDecoration.BOLD)
                        color(NamedTextColor.GOLD)
                        content("➦ ")
                    }
                    appendTextComponent {
                        content("Tarefa Atual")
                    }
                },
                usedLines--
            )
            phoenix.setText(
                textComponent {
                    val taskIndex = PlayerTutorial.TUTORIAL_ORDER.indexOf(activeTutorial::class)
                    var completedTasksCount = taskIndex
                    if (activeTutorial.isCompleted)
                        completedTasksCount++

                    appendTextComponent {
                        color(NamedTextColor.GRAY)
                        content("($completedTasksCount/${PlayerTutorial.TUTORIAL_ORDER.size}) ")
                    }

                    appendTextComponent {
                        color(NamedTextColor.YELLOW)
                        append(activeTutorial.getTaskName())
                    }

                    if (activeTutorial.isCompleted) {
                        appendTextComponent {
                            color(NamedTextColor.GREEN)
                            content(" ✔")
                        }
                    } else {
                        appendTextComponent {
                            color(NamedTextColor.GOLD)
                            content(" ⏳")
                        }
                    }
                },
                usedLines--
            )
            phoenix.setText(textComponent {}, usedLines--)
            if (activeTutorial.isCompleted) {
                phoenix.setText(
                    textComponent {
                        color(NamedTextColor.GRAY)
                        content("*esperando tarefa*")
                    },
                    usedLines--
                )
            } else {
                for (line in activeTutorial.getScoreboardExplanation()) {
                    phoenix.setText(line, usedLines--)
                }
            }

            usedLines
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    fun onFakeGriefPrevention(e: PlayerInteractEvent) {
        val activeTutorial = m.activeTutorials[e.player]?.activeTutorial ?: return

        if (activeTutorial !is SparklyTutorial.ProtectTerrain)
            return

        if (e.action == Action.RIGHT_CLICK_BLOCK && e.item?.type == Material.GOLDEN_SHOVEL) {
            e.isCancelled = true
            if (activeTutorial.isCompleted) {
                e.player.sendMessage(
                    textComponent {
                        color(NamedTextColor.RED)
                        content("Você não pode usar a pá de proteção agora!")
                    }
                )
                return
            }

            if (activeTutorial.playerTutorial.fakeClaimPos1 != null) {
                e.player.sendBlockChange(e.clickedBlock!!.location, Bukkit.createBlockData(Material.GOLD_BLOCK))
                activeTutorial.playerTutorial.fakeClaimPos2 = e.clickedBlock!!.location
                val isValidClaim = activeTutorial.validateClaim()
                if (!isValidClaim) {
                    activeTutorial.playerTutorial.sendMessageAsPantufa(
                        textComponent {
                            content("Hmmm, eu não acho que o terreno que você fez protege a casa inteira... Tente de novo!")
                        }
                    )
                    activeTutorial.playerTutorial.fakeClaimPos1 = null
                    activeTutorial.playerTutorial.fakeClaimPos2 = null
                    return
                }
                activeTutorial.playerTutorial.launchMainThreadTutorialTask {
                    activeTutorial.onComplete()
                }
                return
            }

            activeTutorial.playerTutorial.fakeClaimPos1 = e.clickedBlock!!.location

            BoundaryVisualization.visualizeArea(
                e.player,
                BoundingBox.ofBlocks(listOf(e.clickedBlock)),
                VisualizationType.INITIALIZE_ZONE
            )
        }
    }

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        val playerTutorial = m.activeTutorials[e.player]

        if (playerTutorial != null && e.message.split(" ").first().removePrefix("/").lowercase() !in PlayerTutorial.ALLOWED_COMMANDS) {
            if (e.player.hasPermission("dreamajuda.tutorial.bypasscommands")) {
                e.player.sendMessage(
                    textComponent {
                        color(NamedTextColor.YELLOW)
                        appendTextComponent {
                            content("O comando seria bloqueado pois você está em um tutorial, mas você tem permissão para burlar esta restrição")
                        }
                    }
                )
                return
            }

            e.isCancelled = true
            e.player.sendMessage(
                textComponent {
                    color(NamedTextColor.RED)
                    appendTextComponent {
                        content("Você não pode usar o comando aqui!")
                        appendNewline()
                    }
                    appendTextComponent {
                        appendTextComponent {
                            content("Se você quer pular o tutorial, use ")
                        }
                        appendCommand("/pulartutorial")
                        appendTextComponent {
                            content(", mas não venha chorar falando que você está perdido no servidor, ok?")
                        }
                    }
                }
            )
        }
    }
}