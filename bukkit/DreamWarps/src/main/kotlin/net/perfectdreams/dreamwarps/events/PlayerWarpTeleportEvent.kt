package net.perfectdreams.dreamwarps.events

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerWarpTeleportEvent(val player: Player, val warpName: String, var warpTarget: Location) : Event(), Cancellable {
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