package net.sparklypower.rpc.proxy

import kotlinx.serialization.Serializable
import net.sparklypower.rpc.UUIDAsStringSerializer
import java.util.UUID

@Serializable
data class ProxySendAdminChatRequest(
    val sender: AdminChatSender,
    val rawJsonMessage: String,
    val additionalRawJsonMessageJavaOnlyClient: String?
) : ProxyRPCRequest() {
    @Serializable
    sealed class AdminChatSender {
        @Serializable
        class SparklyUser(
            @Serializable(with = UUIDAsStringSerializer::class)
            val playerUniqueId: UUID
        ) : AdminChatSender()

        @Serializable
        class UnknownUser(
            val displayName: String
        ) : AdminChatSender()
    }
}

@Serializable
sealed class ProxySendAdminChatResponse : ProxyRPCResponse() {
    @Serializable
    data object Success : ProxySendAdminChatResponse()
}