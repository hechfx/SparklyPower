package net.sparklypower.sparklyneonvelocity.commands

import net.kyori.adventure.text.Component
import net.sparklypower.sparklyneonvelocity.utils.commands.context.CommandArguments
import net.sparklypower.sparklyneonvelocity.utils.commands.context.CommandContext
import net.sparklypower.sparklyneonvelocity.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.sparklypower.sparklyneonvelocity.utils.commands.declarations.sparklyCommand
import net.sparklypower.sparklyneonvelocity.utils.commands.executors.SparklyCommandExecutor
import net.sparklypower.sparklyneonvelocity.utils.commands.options.CommandOptions
import net.sparklypower.sparklyneonvelocity.utils.commands.options.SuggestsBlock

class BrigadierVelocityTestCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("brigadiervelocitytest", "bvt")) {
        executor = BrigadierVelocityTestExecutor()
    }

    class BrigadierVelocityTestExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val str = greedyString("str") { source, builder ->
                builder.suggest("loritta")
                builder.suggest("pantufa")
                builder.suggest("gabriela")
            }
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            context.sendMessage(Component.text("hewwo!!! ${args[options.str]}"))
        }
    }
}