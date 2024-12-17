package net.sparklypower.rpc.proxy

import kotlinx.serialization.Serializable
import net.sparklypower.rpc.UUIDAsStringSerializer
import java.util.UUID

@Serializable
data class ProxyExecuteCommandRequest(
    @Serializable(with = UUIDAsStringSerializer::class)
    val playerUniqueId: UUID?,
    val command: String,
) : ProxyRPCRequest()

@Serializable
sealed class ProxyExecuteCommandResponse : ProxyRPCResponse() {
    @Serializable
    data class Success(
        val messages: List<String>,
    ) : ProxyExecuteCommandResponse()
}