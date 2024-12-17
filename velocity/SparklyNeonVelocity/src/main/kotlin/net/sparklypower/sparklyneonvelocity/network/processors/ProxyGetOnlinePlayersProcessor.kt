package net.sparklypower.sparklyneonvelocity.network.processors

import com.velocitypowered.api.proxy.ProxyServer
import io.ktor.server.application.*
import net.sparklypower.rpc.proxy.ProxyGetProxyOnlinePlayersRequest
import net.sparklypower.rpc.proxy.ProxyGetProxyOnlinePlayersResponse
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.network.RPCProcessor
import kotlin.jvm.optionals.getOrNull

class ProxyGetOnlinePlayersProcessor(val m: SparklyNeonVelocity, val server: ProxyServer) : RPCProcessor<ProxyGetProxyOnlinePlayersRequest, ProxyGetProxyOnlinePlayersResponse> {
    override fun process(
        call: ApplicationCall,
        request: ProxyGetProxyOnlinePlayersRequest
    ): ProxyGetProxyOnlinePlayersResponse {
        return ProxyGetProxyOnlinePlayersResponse.Success(
            server.allPlayers.map {
                ProxyGetProxyOnlinePlayersResponse.Success.ProxyPlayer(
                    it.uniqueId,
                    it.username,
                    it.currentServer.getOrNull()?.serverInfo?.name,
                    it.effectiveLocale?.toLanguageTag(),
                    it.ping,
                    it.protocolVersion.protocol,
                    m.isGeyser(it)
                )
            }
        )
    }
}