package net.sparklypower.rpc.proxy

import kotlinx.serialization.Serializable
import net.sparklypower.rpc.UUIDAsStringSerializer
import java.util.UUID

@Serializable
data object ProxyGetProxyOnlinePlayersRequest : ProxyRPCRequest()

@Serializable
sealed class ProxyGetProxyOnlinePlayersResponse : ProxyRPCResponse() {
    @Serializable
    data class Success(val players: List<ProxyPlayer>) : ProxyGetProxyOnlinePlayersResponse() {
        @Serializable
        data class ProxyPlayer(
            @Serializable(with = UUIDAsStringSerializer::class)
            val uniqueId: UUID,
            val name: String,
            val connectedToServerName: String?,
            val locale: String?,
            val ping: Long,
            val protocolVersion: Int,
            val isGeyser: Boolean
        )
    }
}