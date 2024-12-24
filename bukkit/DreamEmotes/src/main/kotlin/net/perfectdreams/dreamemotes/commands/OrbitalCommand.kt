package net.perfectdreams.dreamemotes.commands

import com.charleskorn.kaml.Yaml
import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.papermc.paper.registry.PaperRegistries
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.biome.Biomes
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.sendPacket
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamemotes.DreamEmotes
import net.perfectdreams.dreamemotes.OrbitalCamera
import net.perfectdreams.dreamemotes.StatueBase
import net.sparklypower.rpc.UUIDAsStringSerializer
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.CraftRegistry
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Pig
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.exposed.sql.Table.Dual.text
import org.joml.Matrix4f
import org.joml.Vector3f
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs

class OrbitalCommand(val m: DreamEmotes) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("orbital")) {
        executor = OrbitalExecutor(m)
    }

    class OrbitalExecutor(val m: DreamEmotes) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            if (true) {
                val textDisplay = player.world.spawn(
                    player.location.add(0.0, 5.0, 0.0),
                    TextDisplay::class.java
                ) {
                    it.text(textComponent { content("test scale") })
                    it.teleportDuration = 1
                }

                val inst = AttributeInstance(Attributes.SCALE) {}
                inst.baseValue = 8.0

                val attr = ClientboundUpdateAttributesPacket(
                    textDisplay.entityId,
                    listOf(inst)
                )

                player.sendPacket(attr)
                return
            }

            player.gameMode = GameMode.SPECTATOR

            val textDisplay = player.world.spawn(
                player.location.add(0.0, 5.0, 0.0),
                TextDisplay::class.java
            ) {
                it.teleportDuration = 1
            }

            val spectate = ClientboundSetCameraPacket((player as CraftPlayer).handle)
            // TODO: Add this to the helpful NMS packet changes
            val f = ClientboundSetCameraPacket::class.java.getDeclaredField("cameraId")
            f.isAccessible = true
            f.set(spectate, textDisplay.entityId)

            player.sendPacket(spectate)

            val armorStand = player.world.spawn(
                player.location,
                ArmorStand::class.java
            ) {}

            armorStand.addPassenger(player)

            // val orbitalCamera = OrbitalCamera(m, player, player.location, textDisplay)

            // m.orbitalCameras[player] = orbitalCamera
        }
    }
}