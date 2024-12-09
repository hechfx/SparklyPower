package net.perfectdreams.dreamchat.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Triggered when the player will receive a chat message
 */
class PlayerReceivePlayerChatEvent(
    isAsync: Boolean,
    val sender: ChatSender,
    val receiver: Player
) : Event(isAsync), Cancellable {
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

    sealed class ChatSender {
        data class Player(val player: org.bukkit.entity.Player) : ChatSender()
        data object Bot : ChatSender()
    }
}