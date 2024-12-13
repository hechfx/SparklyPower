package net.perfectdreams.dreamcore.utils.packetevents

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ServerboundPacketReceiveEvent(
    val player: Player,
    var packet: Any
    // The event is always async
) : Event(true), Cancellable {
    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers
    }

    private var isCancelled  = false

    override fun getHandlers(): HandlerList = Companion.handlers

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }
}