package net.perfectdreams.dreamscoreboard.events

import net.perfectdreams.dreamcore.utils.PhoenixScoreboard
import net.perfectdreams.dreamscoreboard.utils.PlayerScoreboard
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerScoreboardRefreshEvent(
    val player: Player,
    val scoreboard: PhoenixScoreboard,
    var block: PlayerScoreboard.() -> (Int)
) : Event() {
    companion object {
        private val HANDLERS_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS_LIST
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS_LIST
    }
}