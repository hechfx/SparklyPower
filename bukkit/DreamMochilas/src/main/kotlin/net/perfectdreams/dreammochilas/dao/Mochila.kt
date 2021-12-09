package net.perfectdreams.dreammochilas.dao

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.fromBase64Inventory
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreammochilas.tables.Mochilas
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level

class Mochila(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Mochila>(Mochilas)

    var owner by Mochilas.owner
    var size by Mochilas.size
    var content by Mochilas.content
    var funnyId by Mochilas.funnyId
    var type by Mochilas.type

    internal var cachedInventory: Inventory? = null
    // Public to allow other plugins locking
    val mochilaInventoryCreationLock = Mutex()
    val mochilaInventoryManipulationLock = Mutex()
    private val locksMutex = Mutex()
    private var locks = 0

    /* fun createMochilaInventory(): Inventory {
        val blahInventory = content.fromBase64Inventory() // Vamos pegar o inventário original

        // E criar ele com o nosso holder personalizado
        val inventory = Bukkit.createInventory(MochilaHolder(this), Math.min(54, size), "§d§lMochila")

        inventory.contents = blahInventory.contents

        return inventory
    } */

    suspend fun getLockCount() = locksMutex.withLock { locks }
    suspend fun lock() = locksMutex.withLock { locks++ }
    suspend fun unlock() {
        locksMutex.withLock {
            val lockCount = locks--
            if (0 > lockCount) {
                try {
                    error("Mochila ${id.value} has less than 0 locks ($lockCount locks)! Bug? We are going to revert it to 0 just to avoid issues but this is a bug that should be fixed!")
                } catch (e: Exception) {
                    MochilaUtils.plugin.logger.log(Level.WARNING, "Mochila ${id.value} unlock issues that should never happen", e)
                    locks = 0
                }
            }
        }
    }

    suspend fun <T> lockForInventoryManipulation(callback: suspend (Inventory) -> (T)): T {
        return mochilaInventoryManipulationLock.withLock {
            callback.invoke(getOrCreateMochilaInventory())
        }
    }

    suspend fun getOrCreateMochilaInventory(): Inventory {
        // We need to lock to avoid two threads loading the inventory at the same time, causing issues
        mochilaInventoryCreationLock.withLock {
            return cachedInventory ?: run {
                val blahInventory = content.fromBase64Inventory() // Vamos pegar o inventário original

                // E criar ele com o nosso holder personalizado
                val inventory = Bukkit.createInventory(MochilaHolder(this), Math.min(54, size), "§d§lMochila")

                val blahInventoryContents = blahInventory.contents
                if (blahInventoryContents != null) {
                    // When serializing, the items are stored as "ItemStack?" if it is AIR
                    // So we are going to workaround this by replacing all null values with a AIR ItemStack!
                    inventory.setContents(
                        blahInventoryContents.map {
                            it ?: ItemStack(Material.AIR)
                        }.toTypedArray()
                    )
                }

                cachedInventory = inventory

                return inventory
            }
        }
    }

    class MochilaHolder(val mochila: Mochila) : InventoryHolder {
        override fun getInventory(): Inventory {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    fun createItem(): ItemStack {
        var item = ItemStack(Material.CARROT_ON_A_STICK)
            .rename("§rMochila")
            .storeMetadata("isMochila", "true")

        val meta = item.itemMeta
        meta as Damageable
        meta.damage = type ?: 1
        item.itemMeta = meta

        val meta2 = item.itemMeta
        meta2.isUnbreakable = true
        item.itemMeta = meta2

        val playerName = Bukkit.getOfflinePlayer(owner)?.name ?: "???"

        item = item.lore(
            "§7Mochila de §b${playerName}",
            "§7",
            "§6${funnyId}"
        ).storeMetadata("mochilaId", id.value.toString())

        return item
    }
}