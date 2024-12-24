package net.perfectdreams.dreamemotes.gestures

import net.perfectdreams.dreamemotes.blockbench.BlockbenchModel

class SparklyGestures(val blockbenchModel: BlockbenchModel) {
    val animations = mutableMapOf<String, SparklyGesture>()

    val clap = create(
        "clap",
        listOf(
            GestureAction.PlayAndLoop(
                getAnimationByName("clap"),
                mapOf(
                    3 to listOf(
                        PlaySound(
                            "minecraft:block.stone.step",
                            1f,
                            1.7f
                        )
                    )
                )
            )
        )
    )

    val oiia = create(
        "oiia",
        listOf(
            GestureAction.Play(
                getAnimationByName("oiia"),
                mapOf(
                    0 to listOf(
                        PlaySound(
                            "sparklypower:general.oiia",
                            1f,
                            1f
                        )
                    )
                )
            )
        )
    )

    val tpose = create(
        "tpose",
        listOf(
            GestureAction.PlayAndHold(getAnimationByName("tpose"), mapOf())
        )
    )

    val disassemble = create(
        "disassemble",
        listOf(
            GestureAction.PlayAndHold(
                getAnimationByName("disassemble"),
                mapOf(
                    0 to listOf(
                        PlaySound(
                            "sparklypower:general.lego_break",
                            1f,
                            1f
                        )
                    )
                )
            )
        )
    )

    private fun getAnimationByName(name: String): BlockbenchModel.Animation {
        return blockbenchModel.animations.first { it.name == name }
    }

    fun create(
        gestureId: String,
        gestureActions: List<GestureAction>
    ): SparklyGesture {
        val gesture = SparklyGesture(
            gestureId,
            gestureActions
        )
        animations[gestureId] = gesture
        return gesture
    }

    sealed class GestureAction {
        abstract val sidecarKeyframes: Map<Int, List<PlaySound>>

        class Play(
            val animation: BlockbenchModel.Animation,
            override val sidecarKeyframes: Map<Int, List<PlaySound>>
        ) : GestureAction()

        class PlayAndHold(
            val animation: BlockbenchModel.Animation,
            override val sidecarKeyframes: Map<Int, List<PlaySound>>
        ) : GestureAction()

        class PlayAndLoop(
            val animation: BlockbenchModel.Animation,
            override val sidecarKeyframes: Map<Int, List<PlaySound>>
        ) : GestureAction()
    }

    class SparklyGesture(
        val id: String,
        val actions: List<GestureAction>
    )

    data class PlaySound(
        val soundKey: String,
        val volume: Float,
        val pitch: Float
    )
}