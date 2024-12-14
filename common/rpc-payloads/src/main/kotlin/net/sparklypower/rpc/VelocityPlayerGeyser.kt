package net.sparklypower.rpc

import kotlinx.serialization.Serializable

@Serializable
sealed class VelocityPlayerGeyserResponse {
    @Serializable
    data class Success(val isGeyser: Boolean) : VelocityPlayerGeyserResponse()

    @Serializable
    data object UnknownPlayer : VelocityPlayerGeyserResponse()
}