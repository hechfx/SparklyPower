package net.sparklypower.sparklyvelocitycore

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.inject.Inject
import com.typesafe.config.ConfigFactory
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.sparklypower.sparklyvelocitycore.commands.VelocityPluginsCommand
import net.sparklypower.sparklyvelocitycore.config.SparklyVelocityCoreConfig
import net.sparklypower.sparklyvelocitycore.pluginmanager.HackyVelocityPluginManager
import net.sparklypower.sparklyvelocitycore.utils.Pudding
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

@Plugin(
    id = "sparklyvelocitycore",
    name = "SparklyVelocityCore",
    version = "1.0.0-SNAPSHOT",
    url = "https://sparklypower.net",
    description = "I did it! Now with more hax!!", // Yay!!!
    authors = ["MrPowerGamerBR"]
)
class SparklyVelocityCore @Inject constructor(val server: ProxyServer, _logger: Logger, @DataDirectory val dataDirectory: Path) : SparklyVelocityPlugin() {
    companion object {
        lateinit var INSTANCE: SparklyVelocityCore
    }

    val logger = _logger

    val pluginManager = HackyVelocityPluginManager(this)
    lateinit var pudding: Pudding

    // This is a bit of another hack, we keep these things in here to avoid getting lost when reloading the SparklyNeonVelocity plugin
    val loggedInPlayers = Collections.newSetFromMap(ConcurrentHashMap<UUID, Boolean>())
    val pingedByAddresses = Collections.newSetFromMap(
        Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build<String, Boolean>()
            .asMap()
    )

    init {
        // A bit of a hack, but that's how it goes
        INSTANCE = this
    }

    // We want to ALWAYS be the last plugin to be called
    @Subscribe(order = PostOrder.CUSTOM, priority = Short.MAX_VALUE)
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val config = Hocon.decodeFromConfig<SparklyVelocityCoreConfig>(ConfigFactory.parseFile(File(dataDirectory.toFile(), "plugin.conf")).resolve())

        this.pudding = Pudding.createPostgreSQLPudding(
            config.database.address,
            config.database.database,
            config.database.username,
            config.database.password,
            128
        )

        for (plugin in server.pluginManager.plugins) {
            // Manually call SparklyPluginLoadEvent
            val instance = plugin.instance.getOrNull()
            logger.info("Plugin ${plugin.description.id} is a ${instance} (${instance is SparklyVelocityPlugin})")
            if (instance is SparklyVelocityPlugin) {
                logger.info("Plugin ${plugin.description.id} is a SparklyVelocityPlugin!")
                instance.onEnable()
            }
        }
    }

    // We want to ALWAYS be the last plugin to be called
    @Subscribe(order = PostOrder.CUSTOM, priority = Short.MAX_VALUE)
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        for (plugin in server.pluginManager.plugins) {
            // Manually call SparklyPluginLoadEvent
            val instance = plugin.instance.getOrNull()
            if (instance is SparklyVelocityPlugin) {
                logger.info("Plugin ${plugin.description.id} is a SparklyVelocityPlugin!")
                instance.onDisable()
            }
        }
    }

    override fun onEnable() {
        registerCommand(server, VelocityPluginsCommand(this))
    }
}