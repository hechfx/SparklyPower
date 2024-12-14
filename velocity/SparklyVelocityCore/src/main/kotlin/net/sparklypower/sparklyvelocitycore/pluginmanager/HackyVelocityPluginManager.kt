package net.sparklypower.sparklyvelocitycore.pluginmanager

import com.google.common.base.Joiner
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.PluginDescription
import com.velocitypowered.api.plugin.PluginManager
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.proxy.VelocityServer
import com.velocitypowered.proxy.plugin.VelocityPluginManager
import com.velocitypowered.proxy.plugin.loader.VelocityPluginContainer
import com.velocitypowered.proxy.plugin.loader.java.JavaPluginLoader
import net.sparklypower.sparklyvelocitycore.SparklyVelocityPlugin
import net.sparklypower.sparklyvelocitycore.SparklyVelocityCore
import java.io.File
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull

class HackyVelocityPluginManager(val m: SparklyVelocityCore) {
    val velocityServer = m.server as VelocityServer
    val basePluginsDirectory = Path.of("plugins")
    val basePluginsDirectoryFile = basePluginsDirectory.toFile()
    //   private final Map<String, PluginContainer> pluginsById = new LinkedHashMap<>();
    //  private final Map<Object, PluginContainer> pluginInstances = new IdentityHashMap<>();
    val PLUGINS_BY_ID_FIELD = VelocityPluginManager::class.java.getDeclaredField("pluginsById").apply {
        this.isAccessible = true
    }
    val PLUGIN_INSTANCES_FIELD = VelocityPluginManager::class.java.getDeclaredField("pluginInstances").apply {
        this.isAccessible = true
    }

    fun loadPlugin(jarFile: File) {
        val velocityPluginManager = velocityServer.pluginManager as VelocityPluginManager
        val foundCandidates: MutableMap<String, PluginDescription> = LinkedHashMap()
        val loader = JavaPluginLoader(velocityServer, basePluginsDirectory)

        val candidate = loader.loadCandidate(jarFile.toPath())

        val alreadyLoadedPluginId = velocityServer.pluginManager.getPlugin(candidate.id).getOrNull()
        if (alreadyLoadedPluginId != null)
            error("Plugin ${candidate.id} is already loaded?!")

        val realPlugin = loader.createPluginFromCandidate(candidate)
        val container = VelocityPluginContainer(realPlugin)
        val module = loader.createModule(container)

        // Make a global Guice module that with common bindings for every plugin
        val commonModule: AbstractModule = object : AbstractModule() {
            override fun configure() {
                bind<ProxyServer>(ProxyServer::class.java).toInstance(velocityServer)
                bind<PluginManager>(PluginManager::class.java).toInstance(velocityServer.pluginManager)
                bind<EventManager>(EventManager::class.java).toInstance(velocityServer.eventManager)
                bind<CommandManager>(CommandManager::class.java).toInstance(velocityServer.commandManager)

                // TODO: How to fix this?
                bind(PluginContainer::class.java)
                    .annotatedWith(Names.named(container.description.id))
                    .toInstance(container)
            }
        }

        val description = container.description

        try {
            loader.createPlugin(container, module, commonModule)
        } catch (e: Throwable) {
            m.logger.error("Can't create plugin {}", description.id, e)
            return
        }

        m.logger.info(
            "Loaded plugin {} {} by {}", description.id, description.version
                .orElse("<UNKNOWN>"), Joiner.on(", ").join(description.authors)
        )
        velocityPluginManager.registerPlugin(container)
        val inst = container.instance.getOrNull()
        if (inst is SparklyVelocityPlugin) {
            inst.onEnable()
        }
    }

    fun unloadPlugin(plugin: PluginContainer) {
        val pluginMain = plugin.instance.getOrNull() ?: error("Plugin Main is null!")
        if (pluginMain is SparklyVelocityPlugin) {
            pluginMain.onDisable()
            velocityServer.eventManager.unregisterListeners(pluginMain)
            for (commandMeta in pluginMain.registeredCommands) {
                velocityServer.commandManager.unregister(commandMeta)
            }
            pluginMain.registeredCommands.clear()
        }

        val velocityPluginManager = velocityServer.pluginManager as VelocityPluginManager
        val pluginsById = PLUGINS_BY_ID_FIELD.get(velocityPluginManager) as MutableMap<String, PluginContainer>
        val pluginInstances = PLUGIN_INSTANCES_FIELD.get(velocityPluginManager) as MutableMap<Object, PluginContainer>

        pluginsById.remove(plugin.description.id, plugin)
        pluginInstances.remove(pluginMain, plugin)
    }
}