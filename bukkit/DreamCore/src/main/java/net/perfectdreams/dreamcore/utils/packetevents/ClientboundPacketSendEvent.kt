package net.perfectdreams.dreamcore.utils.packetevents


import net.minecraft.network.protocol.Packet
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ClientboundPacketSendEvent(
    val player: Player,
    var packet: Any,
    private val packetsThatShouldNotTriggerEvents: MutableList<Packet<*>>
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

    fun sendPacketWithoutTriggeringEvents(packet: Packet<*>) {
        packetsThatShouldNotTriggerEvents.add(packet)
        (player as CraftPlayer).handle.connection.sendPacket(packet)
    }
}