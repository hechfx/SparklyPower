package net.perfectdreams.dreamajuda.cutscenes

import net.perfectdreams.dreamajuda.DreamAjuda
import org.bukkit.GameMode
import org.bukkit.entity.Player

/**
 * A cutscene
 */
abstract class SparklyCutsceneFinalCut(
    val m: DreamAjuda,
    val player: Player,
    val cutsceneCamera: SparklyCutsceneCamera,
    val cutsceneEntityManager: CutsceneEntityManager,
) {
    /**
     * Starts the current cutscene
     */
    abstract suspend fun start()

    /**
     * Cleans up the current cutscene
     */
    open fun cleanUp() {}

    /**
     * Finishes the current cutscene
     */
    fun end(removeActiveCamera: Boolean) {
        cleanUp()

        if (removeActiveCamera) {
            player.gameMode = GameMode.SURVIVAL
            cutsceneCamera.remove()
        }
    }

    /**
     * Runs a specific cutscene
     */
    suspend fun runCutscene(cutscene: SparklyCutsceneFinalCut, removeActiveCamera: Boolean) {
        // Bukkit.broadcastMessage("runCutscene $cutscene - $removeActiveCamera")
        cutscene.start()
        cutscene.end(removeActiveCamera)
    }
}