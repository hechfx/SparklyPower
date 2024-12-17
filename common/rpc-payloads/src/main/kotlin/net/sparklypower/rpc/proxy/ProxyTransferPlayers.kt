package net.sparklypower.rpc.proxy

import kotlinx.serialization.Serializable
import net.sparklypower.rpc.UUIDAsStringSerializer
import java.util.UUID

@Serializable
data class ProxyTransferPlayersRequest(
    val playerUniqueIds: List<@Serializable(with = UUIDAsStringSerializer::class) UUID>,
    val serverName: String
) : ProxyRPCRequest()

@Serializable
sealed class ProxyTransferPlayersResponse : ProxyRPCResponse() {
    @Serializable
    data class Success(
        val playersNotFoundIds: List<@Serializable(with = UUIDAsStringSerializer::class) UUID>
    ) : ProxyTransferPlayersResponse()

    @Serializable
    data object UnknownServer : ProxyTransferPlayersResponse()
}