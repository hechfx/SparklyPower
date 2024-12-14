package net.sparklypower.sparklytestvelocity

import com.google.inject.Inject
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandArguments
import net.sparklypower.sparklyvelocitycore.utils.commands.context.CommandContext
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclaration
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.sparklypower.sparklyvelocitycore.utils.commands.declarations.sparklyCommand
import net.sparklypower.sparklyvelocitycore.utils.commands.executors.SparklyCommandExecutor
import net.sparklypower.sparklyvelocitycore.SparklyVelocityPlugin
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "sparklytestvelocity",
    name = "SparklyTestVelocity",
    version = "1.0.0-SNAPSHOT",
    url = "https://sparklypower.net",
    description = "I did it! Now with more hax!!", // Yay!!!
    authors = ["MrPowerGamerBR"],
    dependencies = arrayOf(
        Dependency(id = "sparklyvelocitycore", optional = false)
    )
)
class SparklyTestVelocity @Inject constructor(val server: ProxyServer, _logger: Logger, @DataDirectory dataDirectory: Path) : SparklyVelocityPlugin() {
    val logger = _logger

    override fun onEnable() {
        logger.info("onEnable() (Sparkly)")
        registerCommand(server, TestCommand())
    }

    override fun onDisable() {

    }

    class TestCommand : SparklyCommandDeclarationWrapper {
        override fun declaration() = sparklyCommand(listOf("testhewwo")) {
            executor = TestHewwoExecutor()
        }

        class TestHewwoExecutor : SparklyCommandExecutor() {
            override fun execute(context: CommandContext, args: CommandArguments) {
                context.sendMessage("§ahewwo ayaya!!!".fromLegacySectionToTextComponent())
            }
        }
    }
}