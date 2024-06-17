package net.perfectdreams.dreamcore.utils.packetevents

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.Packet
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PacketPipelineRegisterListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val nmsPlayer = (player as CraftPlayer).handle

        val packetsThatShouldNotTriggerEvents = mutableListOf<Packet<*>>()

        // Create Netty pipeline to intercept packets
        nmsPlayer
            .connection
            .connection
            .channel
            .pipeline()
            .addBefore(
                "packet_handler",
                "dreamcore-packet-events",
                object: ChannelDuplexHandler() {
                    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
                        try {
                            if (msg in packetsThatShouldNotTriggerEvents) {
                                packetsThatShouldNotTriggerEvents.remove(msg)
                                super.write(ctx, msg, promise)
                                return
                            }

                            val packetSendEvent = ClientboundPacketSendEvent(player, msg, packetsThatShouldNotTriggerEvents)

                            val isNotCancelled = packetSendEvent.callEvent()
                            if (!isNotCancelled)
                                return

                            super.write(ctx, packetSendEvent.packet, promise)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            throw e
                        }
                    }
                }
            )
    }
}