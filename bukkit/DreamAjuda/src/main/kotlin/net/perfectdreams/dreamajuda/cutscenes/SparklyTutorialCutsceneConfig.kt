package net.perfectdreams.dreamajuda.cutscenes

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

@Serializable
data class SparklyTutorialCutsceneConfig(
    val playerMetroChairSittingLocation: Location,
    val boringOfficePlayerChairSittingLocation: Location,
    val boringOfficeCameraStartLocation: Location,
    val boringOfficeCameraFinishLocation: Location,
    val boringOfficeVillagersLocations: List<Location>,
    val metroSoundSourceLocation: Location,
    val metroSoundVolume: Float,
    val metroSoundKey: String,
    val metroNextStationSignLocations: List<Location>,
    val metroNextStationSignScale: Float,
    val metroNextStationCameraLocation: Location,
    val metroNextStationText: String,
    val metroNextStationTextDefault: String,
    val lorittaMessageYouAlwaysDreamedOfCameraStartLocation: Location,
    val lorittaMessageYouAlwaysDreamedOfCameraFinishLocation: Location,
) {
    @Serializable
    data class Location(
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val pitch: Float
    ) {
        fun toBukkitLocation(world: World) = org.bukkit.Location(world, x, y, z, yaw, pitch)
    }
}