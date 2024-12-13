package net.perfectdreams.pantufa.interactions.vanilla.economy

import dev.minn.jda.ktx.interactions.components.asDisabled
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.exceptions.SilentCommandException
import net.perfectdreams.pantufa.dao.CashInfo
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.api.commands.styled
import net.perfectdreams.pantufa.api.minecraft.MinecraftAccountInfo
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.Emotes
import net.perfectdreams.pantufa.utils.extensions.convertShortenedNumberToLong
import net.sparklypower.rpc.TransferCashRequest
import net.sparklypower.rpc.TransferCashResponse
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.NumberFormat
import java.util.*

class PesadelosCommand : SlashCommandDeclarationWrapper {
    companion object {
        private val numberFormat = NumberFormat.getNumberInstance(
            Locale("pt", "BR"),
        )
    }

    override fun command() = slashCommand("pesadelos", "Veja quantos pesadelos você tem!", CommandCategory.ECONOMY) {
        enableLegacyMessageSupport = true

        subcommand("atm", "Veja quantos pesadelos você e outros jogadores do SparklyPower possuem!") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("pesadelos")
            }

            executor = PesadelosAtmCommandExecutor()
        }

        subcommand("pagar", "Transfira pesadelos para outros usuários") {
            requireMinecraftAccount = true

            executor = PesadelosPayCommandExecutor(PantufaBot.INSTANCE)
        }
    }

    inner class PesadelosAtmCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val playerName = optionalString("player_name", "Nome do Player")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val playerName = args[options.playerName]

            if (playerName != null) {
                val playerData = context.pantufa.retrieveMinecraftUserFromUsername(playerName) ?: run {
                    context.reply(false) {
                        styled(
                            "Player desconhecido!",
                            Constants.ERROR
                        )
                    }
                    throw SilentCommandException()
                }

                val playerUniqueId = playerData.id.value

                val cash = transaction(Databases.sparklyPower) {
                    CashInfo.findById(playerUniqueId)
                }?.cash ?: 0

                val formattedCash = numberFormat.format(cash)

                context.reply(false) {
                    styled(
                        "**`${playerData.username}`** possui **${formattedCash} Pesadelos**!",
                        "\uD83D\uDCB5"
                    )
                }
            } else {
                val accountInfo = context.retrieveConnectedMinecraftAccount()!!
                val playerUniqueId = accountInfo.uniqueId

                val cash = transaction(Databases.sparklyPower) {
                    CashInfo.findById(playerUniqueId)
                }?.cash ?: 0

                val formattedCash = numberFormat.format(cash)

                context.reply(false) {
                    styled(
                        "Você possui **${formattedCash} Pesadelos**!",
                        "\uD83D\uDCB5"
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val playerName = args.getOrNull(0)

            return mapOf(
                options.playerName to playerName
            )
        }
    }

    inner class PesadelosPayCommandExecutor(val m: PantufaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val playerName = string("player_name", "Nome do Player") {
                autocomplete {
                    val focusedOptionValue = it.event.focusedOption.value

                    transaction(Databases.sparklyPower) {
                        Users.select(Users.username).where {
                            Users.username.like(focusedOptionValue.replace("%", "") + "%")
                        }
                            .limit(25)
                            .toList()
                    }.associate { it[Users.username] to it[Users.username] }
                }
            }

            val quantity = string("quantity", "Quantidade a ser transferida")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val playerName = args[options.playerName]
            val quantity = args[options.quantity].convertShortenedNumberToLong()

            if (quantity == null || quantity == 0L) {
                context.reply(false) {
                    styled(
                        "Uau, incrível! Você vai transferir *zero* pesadelos, maravilha! Menos trabalho para mim, porque isso significa que não preciso preparar uma transação para você.",
                        Constants.ERROR
                    )
                }
                return
            }

            if (0 > quantity) {
                context.reply(false) {
                    styled(
                        "Uau, excelente! Você vai transferir pesadelos *negativos*, extraordinário! Será que pesadelos negativos seriam... *sonecas*? Caramba, adoro uma sonequinha... `/sonecas`",
                        Constants.ERROR
                    )
                }
                return
            }

            val minecraftAccount = context.retrieveConnectedMinecraftAccountOrFail()

            doTransfer(context, minecraftAccount, playerName, quantity, false)
        }

        private suspend fun doTransfer(context: UnleashedContext, minecraftAccount: MinecraftAccountInfo, receiverName: String, quantity: Long, bypassLastActiveTime: Boolean) {
            val transferCashResponse = Json.decodeFromString<TransferCashResponse>(
                PantufaBot.http.post("${m.config.sparklyPower.server.sparklyPowerSurvival.apiUrl.removeSuffix("/")}/pantufa/transfer-cash") {
                    setBody(
                        Json.encodeToString(
                            TransferCashRequest(
                                minecraftAccount.username,
                                minecraftAccount.uniqueId.toString(),
                                receiverName,
                                quantity,
                                bypassLastActiveTime
                            )
                        )
                    )
                }.bodyAsText()
            )

            when (transferCashResponse) {
                TransferCashResponse.CannotTransferCashToSelf -> {
                    context.reply(false) {
                        styled(
                            "Transferência concluída com sucesso! Você recebeu *nada* de si mesmo, porque você está tentando transferir pesadelos para si mesmo! Se você quer um pesadelo de verdade... d̷̡̨͔̤̯̘̳͚̋͗̃̈́̽̈́̉͂̔͆̈́̕͝͠͝͝u̴̢̳̺̖̳̬̤͖̠̫͖͎̲̲̐͊̈̔̿̈́͌̏́͛̓͘͘̚ŗ̷̡͙͉͈̲͚̾͒̊̍̊̈̉̀̂̉͑̉͐͛̄̚͜m̶̡̥̩̻͔̻̎̌͛̔͌͋ͅầ̷͇̜͍̜̻̒͗̓̐̈̄͋̋̔̾͘ ̶̘̈́̀̌s̷̱̟̙͕̥̤̱͌̌́̔̊́͋̉̓̈͠͠ͅę̸̢̡̢̻͍̟̮̝̺̪̲̰͔̔̌̃́̾͗̂̕͘͠m̴̛̼͙͍̪͙͓͚̞̟͗̏̔̄̏͐͆̌́͆͊̕ ̶̭͉̭̥͓̥̠̖̦̘͂c̷̙̮̗̞̺̳̞͚̯̞̩͚̈̏̀͐̍̽͆̽̓̈̈̋̋͝ͅo̴̢͖͎͓̭̰͍̱͍̭͋̑̉̋̀̐̈́͜͜b̵̢̳̱̖͔͚̪̦͍̥̲̦̭̝̜̝̱̐̈́̒̇̔͆̉̒̅̚̕͠͝e̴͖͑̏̓̑̕̕r̸̢̢͖̬̝̰̱̟̳̫̮͎̾̂̈́̓͑̑͊̌͊͆̓̾͛͠͝͝ţ̸͈̪̻͔͇͈̝̘̥͓̗̼̞̞̙͊̽̐̆̓ͅõ̸͉̰̼͚̳ŕ̷̛̰̝̙͚̗͈̇̈́͒͋̑́̑̐̎̈́́̍̓͆͘.",
                        )
                    }
                }

                is TransferCashResponse.NotEnoughCash -> {
                    context.reply(false) {
                        styled(
                            "Você não possui pesadelos suficientes para transferir! Você possui **${transferCashResponse.currentUserMoney}** ${if (transferCashResponse.currentUserMoney == 1L) "pesadelo" else "pesadelos"}!",
                            Constants.ERROR
                        )
                    }
                }

                TransferCashResponse.PlayerHasNotJoinedRecently -> {
                    context.reply(false) {
                        styled(
                            "O player `${receiverName}` não entra a mais de 14 dias! Você tem certeza que você colocou o nome correto?",
                            Constants.ERROR
                        )

                        styled(
                            "Se você tem certeza que você colocou o nome correto, clique no botão para continuar!",
                            Emotes.PantufaLurk
                        )

                        actionRow(
                            m.interactivityManager.buttonForUser(
                                context.user,
                                ButtonStyle.PRIMARY,
                                "Continuar Transferência"
                            ) { context ->
                                context.editMessage(
                                    true,
                                    MessageEditBuilder.fromMessage(context.event.message)
                                        .apply {
                                            this.setComponents(context.event.message.actionRows.asDisabled())
                                        }
                                        .build()
                                )
                                doTransfer(context, minecraftAccount, receiverName, quantity, true)
                            }
                        )
                    }
                }

                TransferCashResponse.UserDoesNotExist -> {
                    context.reply(false) {
                        styled(
                            "Player não existe! Verifique se você colocou o nome do player corretamente.",
                            Constants.ERROR
                        )
                    }
                }

                is TransferCashResponse.Success -> {

                    context.reply(false) {
                        styled(
                            "Transferência realizada com sucesso! `${transferCashResponse.receiverName}` recebeu **${formatPesadelosAmountWithCurrencyName(transferCashResponse.quantityGiven)}**!",
                            "\uD83E\uDD1D"
                        )

                        styled(
                            "`${minecraftAccount.username}` agora possui ${Emotes.Pesadelos} **${formatPesadelosAmountWithCurrencyName(transferCashResponse.selfMoney)}**!"
                        )

                        styled(
                            "`${transferCashResponse.receiverName}` agora possui ${Emotes.Pesadelos} **${formatPesadelosAmountWithCurrencyName(transferCashResponse.receiverMoney)}**!"
                        )
                    }
                }
            }
        }

        private fun formatPesadelosAmountWithCurrencyName(input: Long): String {
            return if (input == 1L) {
                "$input Pesadelo"
            } else {
                "$input Pesadelos"
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }
}