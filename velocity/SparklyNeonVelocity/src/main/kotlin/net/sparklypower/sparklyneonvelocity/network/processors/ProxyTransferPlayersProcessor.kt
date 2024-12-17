package net.sparklypower.sparklyneonvelocity.network.processors

import com.velocitypowered.api.proxy.ProxyServer
import io.ktor.server.application.*
import net.sparklypower.rpc.proxy.ProxyGetProxyOnlinePlayersRequest
import net.sparklypower.rpc.proxy.ProxyGetProxyOnlinePlayersResponse
import net.sparklypower.rpc.proxy.ProxyTransferPlayersRequest
import net.sparklypower.rpc.proxy.ProxyTransferPlayersResponse
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.network.RPCProcessor
import java.util.*
import kotlin.jvm.optionals.getOrNull

class ProxyTransferPlayersProcessor(val m: SparklyNeonVelocity, val server: ProxyServer) : RPCProcessor<ProxyTransferPlayersRequest, ProxyTransferPlayersResponse> {
    override fun process(
        call: ApplicationCall,
        request: ProxyTransferPlayersRequest
    ): ProxyTransferPlayersResponse {
        val serverInfo = server.getServer(request.serverName).getOrNull() ?: return ProxyTransferPlayersResponse.UnknownServer

        val players = request.playerUniqueIds.mapNotNull { server.getPlayer(it).getOrNull() }

        for (player in players) {
            player.createConnectionRequest(serverInfo).fireAndForget()
        }

        val playersNotFound = request.playerUniqueIds.filter { it !in players.map { it.uniqueId } }

        return ProxyTransferPlayersResponse.Success(playersNotFound)
    }
}