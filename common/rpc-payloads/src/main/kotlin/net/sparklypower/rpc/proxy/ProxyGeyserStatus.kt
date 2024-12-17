package net.sparklypower.rpc.proxy

import kotlinx.serialization.Serializable
import net.sparklypower.rpc.UUIDAsStringSerializer
import java.util.UUID

@Serializable
class ProxyGeyserStatusRequest(@Serializable(with = UUIDAsStringSerializer::class) val playerUniqueId: UUID) : ProxyRPCRequest()

@Serializable
sealed class ProxyGeyserStatusResponse : ProxyRPCResponse() {
    @Serializable
    data class Success(val isGeyser: Boolean) : ProxyGeyserStatusResponse()

    @Serializable
    data object UnknownPlayer : ProxyGeyserStatusResponse()
}