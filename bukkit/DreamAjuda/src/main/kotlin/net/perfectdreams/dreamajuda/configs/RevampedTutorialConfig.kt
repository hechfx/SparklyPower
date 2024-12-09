package net.perfectdreams.dreamajuda.configs

import kotlinx.serialization.Serializable

@Serializable
data class RevampedTutorialConfig(
    val enabled: Boolean,
    val worldName: String,
)