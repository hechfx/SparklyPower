package net.sparklypower.sparklyneonvelocity.network.processors

import com.velocitypowered.api.proxy.ProxyServer
import io.ktor.server.application.*
import net.sparklypower.rpc.proxy.ProxyGeyserStatusRequest
import net.sparklypower.rpc.proxy.ProxyGeyserStatusResponse
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.network.RPCProcessor
import java.util.*
import kotlin.jvm.optionals.getOrNull

class ProxyGeyserStatusProcessor(val m: SparklyNeonVelocity, val server: ProxyServer) : RPCProcessor<ProxyGeyserStatusRequest, ProxyGeyserStatusResponse> {
    override fun process(
        call: ApplicationCall,
        request: ProxyGeyserStatusRequest
    ): ProxyGeyserStatusResponse {
        val playerUniqueId = request.playerUniqueId

        val player = server.getPlayer(playerUniqueId).getOrNull()

        if (player == null)
            return ProxyGeyserStatusResponse.UnknownPlayer

        val isGeyser = m.isGeyser(player)

        return ProxyGeyserStatusResponse.Success(isGeyser)
    }
}