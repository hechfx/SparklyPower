package net.sparklypower.sparklyneonvelocity.network

import com.velocitypowered.api.proxy.ProxyServer
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.sparklypower.rpc.proxy.*
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity

class APIServer(private val plugin: SparklyNeonVelocity, private val velocityServer: ProxyServer) {
    private val logger = plugin.logger
    private var server: EmbeddedServer<*, *>? = null
    val processors = Processors(plugin, velocityServer)

    fun start() {
        logger.info { "Starting HTTP Server..." }
        val server = embeddedServer(Netty, port = plugin.config.rpcPort) {
            routing {
                get("/") {
                    call.respondText("SparklyPower API Web Server")
                }

                post("/rpc") {
                    val jsonPayload = call.receiveText()
                    logger.info { "${call.request.userAgent()} sent a RPC request: $jsonPayload" }

                    val response = when (val request = Json.decodeFromString<ProxyRPCRequest>(jsonPayload)) {
                        is ProxyGeyserStatusRequest -> processors.proxyGeyserStatusProcessor.process(call, request)
                        is ProxyGetProxyOnlinePlayersRequest -> processors.proxyGetOnlinePlayersProcessor.process(call, request)
                        is ProxyTransferPlayersRequest -> processors.proxyTransferPlayersProcessor.process(call, request)
                        is ProxySendAdminChatRequest -> processors.proxySendAdminChatProcessor.process(call, request)
                        is ProxyExecuteCommandRequest -> processors.proxyExecuteCommandProcessor.process(call, request)
                    }

                    call.respondText(
                        Json.encodeToString<ProxyRPCResponse>(response),
                        ContentType.Application.Json
                    )
                }
            }
        }

        // If set to "wait = true", the server hangs
        this.server = server.start(wait = false)
        logger.info { "Successfully started HTTP Server!" }
    }

    fun stop() {
        val server = server
        if (server != null) {
            logger.info { "Shutting down HTTP Server..." }
            server.stop(0L, 5_000) // 5s for timeout
            logger.info { "Successfully shut down HTTP Server!" }
        } else {
            logger.warn { "HTTP Server wasn't started, so we won't stop it..." }
        }
    }
}