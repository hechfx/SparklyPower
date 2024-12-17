package net.perfectdreams.dreamcore.utils

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.DreamCore
import net.sparklypower.rpc.proxy.ProxyRPCRequest
import net.sparklypower.rpc.proxy.ProxyRPCResponse

class RPCUtils(val m: DreamCore) {
    val proxy = ProxyRPCClient()

    class ProxyRPCClient {
        suspend inline fun <reified T : ProxyRPCResponse> makeRPCRequest(request: ProxyRPCRequest): T {
            return Json.decodeFromString<ProxyRPCResponse>(
                DreamUtils.http.post(DreamCore.dreamConfig.servers.bungeeCord.rpcAddress + "/rpc") {
                    setBody(Json.encodeToString<ProxyRPCRequest>(request))
                }.bodyAsText()
            ) as T
        }
    }
}