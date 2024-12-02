package net.perfectdreams.dreamajuda

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import net.perfectdreams.dreamajuda.theatermagic.RecordingPlaybackContext
import net.perfectdreams.dreamajuda.theatermagic.TheaterMagicStoredRecordingAnimation
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.displays.DisplayBlock
import net.perfectdreams.dreamcore.utils.displays.SparklyDisplay
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.*
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.entity.CraftTextDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Husk
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.Transformation
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

class SparklyTutorialCutscene(
    val m: DreamAjuda,
    val player: Player,
    val camera: SparklyCutscene,
    val npc: SparklyNPC
) {
    val hd: SparklyDisplay
    val displayBlock: DisplayBlock.TextDisplayBlock

    init {
        hd = DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(
            m,
            Location(Bukkit.getWorld("RevampedTutorialIsland"), -70.5, 109.45, -62.05, 180f, 0f)
        )
        displayBlock = hd.addDisplayBlock()
    }

    fun spawnSittingNPC() {
        val revampedTutorialIslandWorld = Bukkit.getWorld("RevampedTutorialIsland")!!

        val husk = npc.getEntity()!! as Husk

        husk.equipment.setItem(
            EquipmentSlot.HAND,
            ItemStack.of(Material.PAPER)
                .meta<ItemMeta> {
                    this.setCustomModelData(223)
                }
        )

        val armorStandChair = revampedTutorialIslandWorld.spawnEntity(
            Location(Bukkit.getWorld("RevampedTutorialIsland"), -68.5, 106.5, -86.3, 0.0f, 0f),
            EntityType.TEXT_DISPLAY
        )
        armorStandChair.addPassenger(husk)
    }

    fun createLorittaMessage(): TextDisplay {
        return Bukkit.getWorld("RevampedTutorialIsland")!!.spawn(
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
                font(Key.key("sparklypower", "stuff"))
                content("abcd")
            })
        }
    }

    fun spawnLorittaMessageSelfNPC(): SparklyNPC {
        return DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
            m,
            Location(Bukkit.getWorld("RevampedTutorialIsland"), -74.99907710514692, 85.0, -96.49933552549739, -179.68922f, 0.029606178f),
            player.name,
            null
        )
    }

    fun focusNextStationHologram() {
        camera.setCameraLocation(Location(Bukkit.getWorld("RevampedTutorialIsland"), -70.5, 109.0, -64.5, 0.0f, 0.0f))
    }

    fun easeFromNextStationHologramToBench(): Job {
        return camera.easeCamera(
            m,
            Location(Bukkit.getWorld("RevampedTutorialIsland"), -70.5, 109.0, -64.5, 0.0f, 0.0f),
            Location(Bukkit.getWorld("RevampedTutorialIsland"), -68.5, 107.8, -85.5, 0.0f, 0.00f),
            90
        )
    }

    fun endCutscene() {
        // player.spectatorTarget = null

        player.gameMode = GameMode.SURVIVAL
        player.teleport(Location(Bukkit.getWorld("RevampedTutorialIsland"), -68.5, 107.8, -85.5, 0.0f, 0.00f))
    }

    fun playNextStationHologram(): Job {
        val now = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))

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

        player.playSound(player, "sparklypower:general.metro_paraiso", 1f, 1f)

        val j = m.launchMainThread {
            // TODO: Smooth scroll? There is a width parameter
            val lines = createMarqueeSequence("Próxima Estação: Paraíso. Desembarque pelo lado direito. >>>>>>>>>>".uppercase(), 23) + "${now.dayOfMonth.toString().padStart(2, '0')}/${now.monthValue.toString().padStart(2, '0')}/${now.year} ${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"

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

        return j
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

    fun playbackMiningCutscene(): Job {
        val context = RecordingPlaybackContext(
            m,
            player,
            Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
                File(m.dataFolder, "mining.json").readText()
            ),
            player.world
        ) {
            npc
        }
        return context.startPlayback {}
    }

    fun playbackLeavingHouseCutscene(): Job {
        val context = RecordingPlaybackContext(
            m,
            player,
            Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
                File(m.dataFolder, "leaving_house.json").readText()
            ),
            player.world
        ) {
            npc
        }
        return context.startPlayback {}
    }

    suspend fun playWhiteFadeTitleAndWait() {
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

        delay(Ticks.SINGLE_TICK_DURATION_MS * 5)
    }
}