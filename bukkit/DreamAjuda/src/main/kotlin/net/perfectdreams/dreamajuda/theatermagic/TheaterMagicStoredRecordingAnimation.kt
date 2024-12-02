package net.perfectdreams.dreamajuda.theatermagic

import kotlinx.serialization.Serializable

@Serializable
class TheaterMagicStoredRecordingAnimation(
    val keyframes: Map<Int, RecordedKeyframe>
) {
    @Serializable
    data class RecordedKeyframe(val actions: List<AnimationAction>)
}