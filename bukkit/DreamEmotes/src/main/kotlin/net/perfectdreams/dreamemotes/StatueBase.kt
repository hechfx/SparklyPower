package net.perfectdreams.dreamemotes

import kotlinx.serialization.Serializable

@Serializable
data class StatueBase(
    val torsoTop: StatuePart,
    val torsoBottom: StatuePart,
) {
    @Serializable
    data class StatuePart(
        val front: SkinPart,
        val frontSecondaryLayer: SkinPart,

        val back: SkinPart,
        val backSecondaryLayer: SkinPart,

        val left: SkinPart,
        val leftSecondaryLayer: SkinPart,

        val right: SkinPart,
        val rightSecondaryLayer: SkinPart,

        val top: SkinPart,
        val topSecondaryLayer: SkinPart,

        val bottom: SkinPart,
        val bottomSecondaryLayer: SkinPart,
    ) {
        @Serializable
        data class SkinPart(
            val x: Int,
            val y: Int,
            val width: Int,
            val height: Int
        )
    }
}