package net.perfectdreams.dreamajuda.theatermagic

import com.sk89q.worldedit.regions.Region
import net.perfectdreams.dreamajuda.DreamAjuda
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

class TheaterMagicManager(val m: DreamAjuda) {
    val recordingAnimations = mutableMapOf<Player, TheaterMagicRecordingAnimation>()

    fun getActiveRecording(player: Player) = recordingAnimations[player]

    fun startRecording(
        player: Player,
        region: Region
    ): TheaterMagicRecordingAnimation {
        recordingAnimations[player]?.finish()

        val animation = TheaterMagicRecordingAnimation(
            m,
            player,
            player.world,
            Location(
                player.world,
                region.minimumPoint.x.toDouble(),
                region.minimumPoint.y.toDouble(),
                region.minimumPoint.z.toDouble(),
            ),
            Location(
                player.world,
                region.maximumPoint.x.toDouble(),
                region.maximumPoint.y.toDouble(),
                region.maximumPoint.z.toDouble(),
            ),
            Bukkit.getCurrentTick()
        )

        recordingAnimations[player] = animation

        animation.start()

        return animation
    }
}