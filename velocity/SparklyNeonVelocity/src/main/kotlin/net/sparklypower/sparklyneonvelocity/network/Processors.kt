package net.sparklypower.sparklyneonvelocity.network

import com.velocitypowered.api.proxy.ProxyServer
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.network.processors.*

class Processors(m: SparklyNeonVelocity, server: ProxyServer) {
    val proxyGeyserStatusProcessor = ProxyGeyserStatusProcessor(m, server)
    val proxyGetOnlinePlayersProcessor = ProxyGetOnlinePlayersProcessor(m, server)
    val proxyTransferPlayersProcessor = ProxyTransferPlayersProcessor(m, server)
    val proxySendAdminChatProcessor = ProxySendAdminChatProcessor(m, server)
    val proxyExecuteCommandProcessor = ProxyExecuteCommandProcessor(m, server)
}