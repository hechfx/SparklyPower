package net.perfectdreams.dreamajuda.theatermagic

import kotlinx.serialization.Serializable

@Serializable
data class StoredBlockData(
    val x: Int,
    val y: Int,
    val z: Int,
    val blockData: String
)