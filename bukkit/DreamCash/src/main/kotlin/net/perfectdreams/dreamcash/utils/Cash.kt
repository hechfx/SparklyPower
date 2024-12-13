package net.perfectdreams.dreamcash.utils

import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcash.DreamCash
import net.perfectdreams.dreamcash.dao.CashInfo
import net.perfectdreams.dreamcash.tables.Cashes
import net.perfectdreams.dreamcore.cash.NightmaresCashRegister
import net.perfectdreams.dreamcore.tables.TrackedOnlineHours
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import org.bukkit.Bukkit
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.time.Instant
import java.util.*

object Cash : NightmaresCashRegister {
    override fun giveCash(player: Player, quantity: Long, transactionContext: TransactionContext) =
        giveCash(player.uniqueId, quantity, transactionContext)

    override fun giveCash(uniqueId: UUID, quantity: Long, transactionContext: TransactionContext) {
        if (0 >= quantity)
            throw IllegalArgumentException("Quantity is less or equal to zero! quantity = $quantity")

        transaction(Databases.databaseNetwork) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(uniqueId) ?: CashInfo.new(uniqueId) {
                    this.cash = 0
                }
            }

            cashInfo.cash += quantity

            transactionContext.apply {
                receiver = uniqueId
                currency = TransactionCurrency.CASH
                amount = quantity.toDouble()
            }.saveToDatabase()
        }
    }

    override fun takeCash(player: Player, quantity: Long, transactionContext: TransactionContext) =
        takeCash(player.uniqueId, quantity, transactionContext)

    override fun takeCash(uniqueId: UUID, quantity: Long, transactionContext: TransactionContext) {
        transaction(Databases.databaseNetwork) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(uniqueId) ?: CashInfo.new(uniqueId) {
                    this.cash = 0
                }
            }

            if (quantity > cashInfo.cash)
                throw IllegalArgumentException("Quantity is more than player has! quantity = $quantity cashInfo.cash = ${cashInfo.cash}")

            cashInfo.cash -= quantity

            transactionContext.apply {
                payer = uniqueId
                currency = TransactionCurrency.CASH
                amount = quantity.toDouble()
            }.saveToDatabase()
        }
    }

    override fun setCash(player: Player, quantity: Long) = setCash(player.uniqueId, quantity)

    override fun setCash(uniqueId: UUID, quantity: Long) {
        if (0 > quantity)
            throw IllegalArgumentException("New quantity is less than zero! quantity = $quantity")

        transaction(Databases.databaseNetwork) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(uniqueId) ?: CashInfo.new(uniqueId) {
                    this.cash = 0
                }
            }

            cashInfo.cash = quantity
        }
    }

    override fun getCash(player: Player) = getCash(player.uniqueId)

    override fun getCash(uniqueId: UUID): Long {
        return transaction(Databases.databaseNetwork) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(uniqueId)
            }

            cashInfo?.cash ?: 0
        }
    }

    /**
     * Transfers cash from one player to another
     */
    suspend fun transferCashFromPlayerToPlayer(
        giverName: String,
        giverUniqueId: UUID,
        receiverName: String,
        quantity: Long,
        bypassLastActiveTime: Boolean
    ): TransferCashResult {
        return net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
            // Does the other account exist?
            val receiverData = Users.selectAll().where { Users.username eq receiverName }.firstOrNull()

            // The other user does not exist at all!
            if (receiverData == null)
                return@transaction TransferCashResult.UserDoesNotExist

            return@transaction _transferCashFromPlayerToPlayer(
                giverUniqueId,
                receiverData,
                quantity,
                bypassLastActiveTime
            )
        }.also { postTransfer(giverName, giverUniqueId, it) }
    }

    private fun postTransfer(giverName: String, giverUniqueId: UUID, result: TransferCashResult) {
        if (result is TransferCashResult.Success) {
            TransactionContext(
                currency = TransactionCurrency.CASH,
                payer = giverUniqueId,
                receiver = result.receiverId,
                type = TransactionType.PAYMENT,
                amount = result.quantityGiven.toDouble()
            ).saveToDatabase()

            val receiverPlayer = Bukkit.getPlayer(result.receiverId) ?: return

            receiverPlayer.sendMessage(
                textComponent {
                    append(DreamCash.PREFIX)
                    appendSpace()
                    append("Você recebeu §c${result.quantityGiven} pesadelos§a de §b${giverName}§a, agora você tem §c${result.receiverMoney} pesadelos§a! Então, que tal comprar VIP? §6/lojacash") {
                        color(NamedTextColor.GREEN)
                    }
                }
            )

            receiverPlayer.playSound(receiverPlayer.location, "sparklypower.sfx.money", SoundCategory.RECORDS, 1.0f, DreamUtils.random.nextFloat(0.9f, 1.1f))
        }
    }

    private fun _transferCashFromPlayerToPlayer(
        giverUniqueId: UUID,
        receiverData: ResultRow,
        quantity: Long,
        bypassLastActiveTime: Boolean
    ): TransferCashResult {
        // You can't transfer cash to yourself!
        if (receiverData[Users.id].value == giverUniqueId)
            return TransferCashResult.CannotTransferCashToSelf

        // Do we have enough money?
        val selfCashes = Cashes.selectAll().where { Cashes.id eq giverUniqueId }.firstOrNull()?.get(Cashes.cash) ?: 0L

        if (quantity > selfCashes) {
            // You don't have enough cash!
            return TransferCashResult.NotEnoughCash(selfCashes)
        }

        // Before we transfer, we will check if the user has logged in for the last 14 days, if they haven't, we will send a warning to them
        if (!bypassLastActiveTime) {
            // When was the last time that player logged in?
            val lastTimeThatTheUserLoggedIn = TrackedOnlineHours.select(TrackedOnlineHours.loggedIn).where {
                TrackedOnlineHours.player eq receiverData[Users.id].value
            }.orderBy(TrackedOnlineHours.loggedIn, SortOrder.DESC)
                .limit(1)
                .firstOrNull()
                ?.get(TrackedOnlineHours.loggedIn)

            if (lastTimeThatTheUserLoggedIn == null || lastTimeThatTheUserLoggedIn.isBefore(Instant.now().minusSeconds(86400 * 14))) { // 14 days
                return TransferCashResult.PlayerHasNotJoinedRecently
            }
        }

        // We need to manually create the account if the user does not have a cash account yet
        // We don't need to create an account for the current user because if the player has != 0.0 then it means that they have an account already
        if (Cashes.selectAll().where { Cashes.id eq receiverData[Users.id].value }.count() == 0L) {
            Cashes.insert {
                it[Cashes.id] = receiverData[Users.id].value
                it[Cashes.cash] = 0L
            }
        }

        // Take money!!!
        val selfUpdateReturningStatement = Cashes.updateReturning(listOf(Cashes.cash), { Cashes.id eq giverUniqueId }) {
            with(SqlExpressionBuilder) {
                it[Cashes.cash] = Cashes.cash - quantity
            }
        }.first()

        val selfMoney = selfUpdateReturningStatement[Cashes.cash]

        // Give money!!!
        val receiverUpdateReturningStatement = Cashes.updateReturning(listOf(Cashes.cash), { Cashes.id eq receiverData[Users.id] }) {
            with(SqlExpressionBuilder) {
                it[Cashes.cash] = Cashes.cash + quantity
            }
        }.first()

        val receiverMoney = receiverUpdateReturningStatement[Cashes.cash]

        return TransferCashResult.Success(
            receiverData[Users.username],
            receiverData[Users.id].value,
            quantity,
            selfMoney,
            receiverMoney,
        )
    }

    sealed class TransferCashResult {
        data object CannotTransferCashToSelf : TransferCashResult()
        data object PlayerHasNotJoinedRecently : TransferCashResult()
        data object UserDoesNotExist : TransferCashResult()
        data class NotEnoughCash(val currentUserMoney: Long) : TransferCashResult()
        data class Success(
            val receiverName: String,
            val receiverId: UUID,
            val quantityGiven: Long,
            val selfMoney: Long,
            val receiverMoney: Long,
        ) : TransferCashResult()
    }
}