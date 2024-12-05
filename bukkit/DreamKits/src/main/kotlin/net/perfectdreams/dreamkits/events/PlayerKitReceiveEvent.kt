package net.perfectdreams.dreamkits.events

import net.perfectdreams.dreamkits.utils.Kit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerKitReceiveEvent(val player: Player, val kit: Kit) : Event(), Cancellable {
    companion object {
        private val HANDLERS_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS_LIST
        }
    }

    private var isCancelled = false

    override fun getHandlers(): HandlerList {
        return HANDLERS_LIST
    }

    override fun isCancelled() = isCancelled

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }
}