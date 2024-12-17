package net.sparklypower.sparklyneonvelocity.network

import io.ktor.server.application.*

interface RPCProcessor<Req, Res> {
    fun process(call: ApplicationCall, request: Req): Res
}