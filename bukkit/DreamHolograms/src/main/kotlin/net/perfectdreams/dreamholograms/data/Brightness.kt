package net.perfectdreams.dreamholograms.data

import kotlinx.serialization.Serializable

@Serializable
data class Brightness(
    val blockLight: Int,
    val skyLight: Int
)