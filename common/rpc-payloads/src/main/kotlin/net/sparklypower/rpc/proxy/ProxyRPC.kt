package net.sparklypower.rpc.proxy

import kotlinx.serialization.Serializable

@Serializable
sealed class ProxyRPCRequest

@Serializable
sealed class ProxyRPCResponse