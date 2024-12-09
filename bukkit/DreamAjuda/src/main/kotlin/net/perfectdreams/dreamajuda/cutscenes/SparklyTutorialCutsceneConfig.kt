package net.perfectdreams.dreamajuda.cutscenes

import kotlinx.serialization.Serializable
import net.perfectdreams.dreamcore.serializable.SerializedBlockPosition
import net.perfectdreams.dreamcore.serializable.SerializedLocation
import net.perfectdreams.dreamcore.serializable.SerializedWorldLocation

@Serializable
data class SparklyTutorialCutsceneConfig(
    val playerCutsceneFinishLocation: SerializedLocation,
    val playerMetroChairSittingLocation: SerializedLocation,
    val boringOfficePlayerChairSittingLocation: SerializedLocation,
    val boringOfficeCameraStartLocation: SerializedLocation,
    val boringOfficeCameraFinishLocation: SerializedLocation,
    val boringOfficeVillagersLocations: List<SerializedLocation>,
    val metroSoundSourceLocation: SerializedLocation,
    val metroSoundVolume: Float,
    val metroSoundKey: String,
    val metroNextStationSignLocations: List<SerializedWorldLocation>,
    val metroNextStationSignScale: Float,
    val metroNextStationCameraLocation: SerializedLocation,
    val metroNextStationText: String,
    val metroNextStationTextDefault: String,
    val lorittaMessageYouAlwaysDreamedOfCameraStartLocation: SerializedLocation,
    val lorittaMessageYouAlwaysDreamedOfCameraFinishLocation: SerializedLocation,
    val leaveYourMark: LeaveYourMarkConfig,
    val lifetimeFriends: LifetimeFriends,
    val blackWalls: List<BlackWalls>,
    val metroDoors: List<SerializedLocation>
) {
    @Serializable
    data class LeaveYourMarkConfig(
        val playerChairSittingLocation: SerializedLocation,
        val cameraStartLocation: SerializedLocation,
        val cameraFinishLocation: SerializedLocation,
    )

    @Serializable
    data class LifetimeFriends(
        val cameraLocation: SerializedLocation,
        val skin2: SkinTexture?,
        val skin3: SkinTexture?,
        val skin4: SkinTexture?
    ) {
        @Serializable
        data class SkinTexture(
            val value: String,
            val signature: String
        )
    }

    @Serializable
    data class BlackWalls(
        val area1: SerializedBlockPosition,
        val area2: SerializedBlockPosition
    )
}