package net.perfectdreams.pantufa.utils

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.pantufa.PantufaBot
import net.sparklypower.rpc.proxy.ProxyRPCRequest
import net.sparklypower.rpc.proxy.ProxyRPCResponse

class ProxyRPCClient(val pantufa: PantufaBot) {
    suspend inline fun <reified T : ProxyRPCResponse> makeRPCRequest(request: ProxyRPCRequest): T {
        return Json.decodeFromString<ProxyRPCResponse>(
            PantufaBot.http.post("${pantufa.config.sparklyPower.server.sparklyPowerProxy.apiUrl.removeSuffix("/")}/rpc") {
                setBody(Json.encodeToString<ProxyRPCRequest>(request))
            }.bodyAsText()
        ) as T
    }
}