package net.sparklypower.rpc

import kotlinx.serialization.Serializable

@Serializable
data class TransferCashRequest(
    val giverName: String,
    val requestedById: String,
    val receiverName: String,
    val quantity: Long,
    val bypassLastActiveTime: Boolean
)

@Serializable
sealed class TransferCashResponse {
    @Serializable
    data object CannotTransferCashToSelf : TransferCashResponse()

    @Serializable
    data object PlayerHasNotJoinedRecently : TransferCashResponse()

    @Serializable
    data object UserDoesNotExist : TransferCashResponse()

    @Serializable
    data object YouAreBanned : TransferCashResponse()

    @Serializable
    data object YouAreTryingToTransferToABannedUser : TransferCashResponse()

    @Serializable
    data class NotEnoughCash(val currentUserMoney: Long) : TransferCashResponse()

    @Serializable
    data class Success(
        val receiverName: String,
        val receiverId: String,
        val quantityGiven: Long,
        val selfMoney: Long,
        val receiverMoney: Long,
    ) : TransferCashResponse()
}
