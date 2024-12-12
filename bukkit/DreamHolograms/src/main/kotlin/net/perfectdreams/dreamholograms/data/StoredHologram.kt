package net.perfectdreams.dreamholograms.data

import kotlinx.serialization.Serializable
import net.perfectdreams.dreamcore.serializable.SerializedWorldLocation

// Everything is MUTABLE because that's easier for us to manage
@Serializable
data class StoredHologram(
    var location: SerializedWorldLocation,
    var lines: MutableList<HologramLine>
) {
    companion object {
        const val DEFAULT_BACKGROUND_COLOR = 1073741824
    }
}