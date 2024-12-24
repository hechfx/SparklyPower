package net.perfectdreams.dreamemotes.config

import kotlinx.serialization.Serializable

@Serializable
data class DreamEmotesConfig(
    val allowedWorlds: List<String>,
    val mineskinApi: MineSkinConfig
) {
    @Serializable
    data class MineSkinConfig(
        val apiKey: String
    )
}