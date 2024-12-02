package net.perfectdreams.dreamajuda

import kotlinx.serialization.Serializable

@Serializable
class RecordedAnimation(
    val startLocation: AbsoluteLocation,
    val totalAnimationDuration: Int,
    var keyframes: Map<Int, MutableList<AnimationAction>>
)