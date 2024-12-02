package net.perfectdreams.dreamajuda

import kotlinx.coroutines.Job
import org.bukkit.Location

class SparklyAnimation(
    val m: PathKeyframerManager,
    val startedAtTick: Int,
    val startLocation: Location,
    var keyframes: MutableMap<Int, MutableList<AnimationAction>>
) {
    var job: Job? = null

    fun addAction(currentTick: Int, action: AnimationAction) {
        keyframes.getOrPut(currentTick - startedAtTick) { mutableListOf() }.add(action)
    }

    fun finish(): RecordedAnimation {
        job?.cancel()

        return RecordedAnimation(
            AbsoluteLocation.toAbsoluteLocation(startLocation),
            keyframes.keys.max(),
            keyframes
        )
    }
}