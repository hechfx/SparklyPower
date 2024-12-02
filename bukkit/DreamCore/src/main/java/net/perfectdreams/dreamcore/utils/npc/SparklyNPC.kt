package net.perfectdreams.dreamcore.utils.npc

import io.papermc.paper.entity.TeleportFlag
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Husk
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.util.UUID
import kotlin.experimental.or

class SparklyNPC(
    val m: SparklyNPCManager,
    val owner: Plugin,
    var name: String,
    val fakePlayerName: String,
    val initialLocation: Location,
    var textures: SkinTexture?,
    // We can't (and shouldn't!) store the entity reference, since the reference may change when the entity is despawned!
    // So we store the unique ID
    val uniqueId: UUID
) {
    private val PLAYER_EQUIPMENT_SLOTS = setOf(
        EquipmentSlot.HAND,
        EquipmentSlot.OFF_HAND,
        EquipmentSlot.FEET,
        EquipmentSlot.LEGS,
        EquipmentSlot.CHEST,
        EquipmentSlot.HEAD,
    )

    internal var onLeftClickCallback: ((Player) -> (Unit))? = null
    internal var onRightClickCallback: ((Player) -> (Unit))? = null
    var lookClose = false
    var location: Location = initialLocation
    var pose = Pose.STANDING
        private set
    var isSneaking = false
        private set
    var isSprinting = false
        private set
    var nameplateVisibility = Team.OptionStatus.ALWAYS
        private set
    val equipment = NPCEquipment(this)
    private var locationHasBeenUpdated = true

    /**
     * Gets the NPC entity, this may be null if the entity is unloaded
     */
    fun getEntity() = Bukkit.getEntity(uniqueId) as Husk?

    /**
     * Callback that will be invoked when clicking the NPC
     */
    fun onClick(callback: ((Player) -> (Unit))?) {
        onLeftClickCallback = callback
        onRightClickCallback = callback
    }

    /**
     * Teleports the NPC to the new [location]
     */
    fun teleport(location: Location) {
        this.location = location
        this.locationHasBeenUpdated = false

        getAndUpdateEntity()
    }

    /**
     * Deletes the NPC from the world
     */
    fun remove() {
        m.m.logger.info { "Removing NPC ${uniqueId} from the world and from the NPC storage..." }
        m.npcEntities.remove(uniqueId)
        getEntity()?.remove()
        m.deleteFakePlayerName(this)
    }

    /**
     * Sets the player name
     */
    fun setPlayerName(name: String) {
        this.name = name

        m.updateFakePlayerName(this)
    }

    /**
     * Sets the player's skin textures
     */
    fun setPlayerTextures(textures: SkinTexture?) {
        this.textures = textures

        // When changing the texture, we need to hide and unhide the entity for all players, to resend the player list packet
        val bukkitEntity = getEntity() ?: return // Nevermind...
        Bukkit.getOnlinePlayers().forEach {
            it.hideEntity(m.m, bukkitEntity)
            it.showEntity(m.m, bukkitEntity)
        }
    }

    internal fun updateName(scoreboard: Scoreboard) {
        val length = name.length
        val midpoint = length / 2
        val firstHalf = name.substring(0, midpoint)
        val secondHalf = name.substring(midpoint, length)

        val teamName = SparklyNPCManager.getTeamName(uniqueId)
        val t = scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)
        t.prefix = firstHalf
        t.suffix = secondHalf

        // Bukkit.broadcastMessage("First half is: $firstHalf")
        // Bukkit.broadcastMessage("Second half is: $secondHalf")
        t.setOption(Team.Option.NAME_TAG_VISIBILITY, nameplateVisibility)

        // "Identifiers for the entities in this team. For players, this is their username; for other entities, it is their UUID." - wiki.vg
        t.addEntry(fakePlayerName)
    }

    private fun getAndUpdateEntity() {
        val entity = getEntity() ?: return
        updateEntity(entity)
    }

    fun updateEntity(husk: Husk) {
        if (!locationHasBeenUpdated) {
            husk.teleport(this.location)
            this.locationHasBeenUpdated = true
        }

        husk.bodyYaw = this.location.yaw
        husk.setPose(pose, true)
        husk.isSneaking = true
        for (slot in PLAYER_EQUIPMENT_SLOTS) {
            husk.equipment.setItem(slot, equipment.items[slot])
        }
    }

    fun setPose(pose: Pose) {
        this.pose = pose
        getAndUpdateEntity()
    }

    fun setSneaking(state: Boolean) {
        this.isSneaking = state
        getAndUpdateEntity()
    }

    fun createMetadata(): List<SynchedEntityData.DataValue<out Any>> {
        // Define flags
        val isOnFire = 0x01.toByte()  // 1
        val isCrouching = 0x02.toByte()  // 2
        val unused = 0x04.toByte()  // 4
        val isSprinting = 0x08.toByte()  // 8
        val isSwimming = 0x10.toByte()  // 16
        val isInvisible = 0x20.toByte()  // 32
        val hasGlowingEffect = 0x40.toByte()  // 64
        val isFlyingWithElytra = 0x80.toByte()  // 128

        // Combine flags using OR
        var playerState = 0.toByte()
        if (this.isSneaking) {
            playerState = playerState or isCrouching
        }
        if (this.isSprinting) {
            playerState = playerState or isSprinting
        }

        return listOf(
            SynchedEntityData.DataValue(0, EntityDataSerializers.BYTE, playerState),
            SynchedEntityData.DataValue(6, EntityDataSerializers.POSE, net.minecraft.world.entity.Pose.entries[pose.ordinal]),
            SynchedEntityData.DataValue(17, EntityDataSerializers.BYTE, 127.toByte()),
        )
    }

    fun swingMainHand() {
        getEntity()?.swingMainHand()
    }

    fun swingOffHand() {
        getEntity()?.swingOffHand()
    }

    fun swingHand(slot: EquipmentSlot) {
        getEntity()?.swingHand(slot)
    }

    fun setNameplateVisibility(status: Team.OptionStatus) {
        this.nameplateVisibility = status
        m.updateFakePlayerName(this)
    }

    class NPCEquipment(val npc: SparklyNPC) {
        val items = mutableMapOf<EquipmentSlot, ItemStack>()

        fun setItem(slot: EquipmentSlot, item: ItemStack?) {
            if (item == null)
                items.remove(slot)
            else
                items[slot] = item

            npc.getAndUpdateEntity()
        }
    }
}