package net.sparklypower.sparklyneonvelocity.network.processors

import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJsonArray
import com.velocitypowered.api.permission.Tristate
import com.velocitypowered.api.proxy.ProxyServer
import io.ktor.server.application.*
import net.kyori.adventure.util.TriState
import net.sparklypower.rpc.proxy.*
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.User
import net.sparklypower.sparklyneonvelocity.network.RPCProcessor
import net.sparklypower.sparklyneonvelocity.utils.socket.SocketServer.FakeCommandPlayerSender
import net.sparklypower.sparklyneonvelocity.utils.socket.SocketServer.FakeCommandSender
import java.util.*
import kotlin.jvm.optionals.getOrNull

class ProxyExecuteCommandProcessor(val m: SparklyNeonVelocity, val server: ProxyServer) : RPCProcessor<ProxyExecuteCommandRequest, ProxyExecuteCommandResponse> {
    override fun process(
        call: ApplicationCall,
        request: ProxyExecuteCommandRequest
    ): ProxyExecuteCommandResponse {
        val playerUniqueId = request.playerUniqueId
        val command = request.command

        val commandSender = if (playerUniqueId != null) {
            val user = m.pudding.transactionBlocking { User.findById(playerUniqueId) }!!
            FakeCommandPlayerSender(user.username, playerUniqueId)
        } else {
            FakeCommandSender()
        }

        if (commandSender is FakeCommandPlayerSender) {
            m.logger.info { "Dispatching command $command by ${commandSender.username} (${commandSender.uniqueId})!" }
        } else {
            m.logger.info { "Dispatching command $command by *unknown*!" }
        }

        server.commandManager.executeAsync(
            commandSender,
            command
        ).get()

        m.logger.info { "Command Output is ${commandSender.output}" }
        return ProxyExecuteCommandResponse.Success(commandSender.output)
    }
}