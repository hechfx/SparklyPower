package net.perfectdreams.dreamajuda.tutorials

import kotlinx.coroutines.Job
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentLinkedQueue

class PlayerTutorial(
    val m: DreamAjuda,
    val player: Player
) {
    companion object {
        val ALLOWED_COMMANDS = setOf(
            "warp",
            "tutorialtester",
            "kit",
            "skiptutorial",
            "pulartutorial",
            "sairtutorial"
        )

        val TUTORIAL_ORDER = listOf(
            SparklyTutorial.LeaveTheSubway::class,
            SparklyTutorial.KitNoob::class,
            SparklyTutorial.WarpResources::class,
            SparklyTutorial.WarpSurvival::class,
            SparklyTutorial.ProtectTerrain::class
        )
    }

    lateinit var activeTutorial: SparklyTutorial
    private val activeJobs = ConcurrentLinkedQueue<Job>()

    val pantufaNPC = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
        m,
        // Spawn the NPC somewhere
        player.location.clone().apply {
            this.x = -115.0
            this.y = 106.0
            this.z = 75.0
        },
        "Pantufa",
        SkinTexture(
            "ewogICJ0aW1lc3RhbXAiIDogMTczMzMzMTA3ODE1MywKICAicHJvZmlsZUlkIiA6ICJiODM3ZjNkNjc3MjA0YjQ2OWE4ZTJiNmJmMDRhMjQxYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJQYW50dWZpbmhhIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlkZDEyNTZkNmI5MjNkM2FlNjE4ZjVmMTNmZGEwYTY0MGY2ZTQ1ODVhZTNjMzUzZDg1ZTlmZTY1ODk5NmM4YWEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
            "BA4XB8m7flK+y49saK+IRV4qDPMIAUp+cavVzufhaQAHKZs06qUCNp96MmA5z5Vl6/LsVkRDuLqEhAHE01F82QZmk3bSfayKPN8EJmHeMKuHR7Rf6GsmEP7Rn9Dl8sMOJwWgaR/EzRVwPAiUkSyb5vMa1KLKVqhqQB9V6g+OCVCpIBHLR+vpJ4Xgw0WFCd9XafoRVjbGMFKmZutIvkaN0n8BRKRHtOgaLxYL4C/h5jFY43kiCN602GbbNUmjlyHy53EoFMW7xQw1tiXbaO/vcyvzYQjZPWXIxkNXGIZ/gLdUKIbcNr6WVYGIpSQeklWo7wWHLS0Bbu6VDIBf5XW1yTRudcuByu0KRLdpywk436B9Ro+j1oIcdvob499R/+7OqFa3kxJKg16tHyOI8sym9soQlmV/C6VZWb8PIsG5w+CGNAXsOuESR3UucFGLeS08sNRYJgDzte3fdVXK3JgWOW8Sl2sbt86N9lJG80KnW6YyWH7KHh2RsRbtW2vuZcFBXNHulp2e61TTCQ6dV2oqJx0EkUBKNZL6rC4EysRklE8sWdQJtrfiLV8e6LcA+R3a9Mdc7JNbX1TteZx7LqmO3mHryRJ7KZVn11xQPRzVO+LN56/dW3XdIHpwOlnQcpLUUFYgDZtNfG1dkS/9v295Xz5HRqHU7r5szLLx/Cqrrt0="
        )
    ) { entity, npc ->
        npc.setVisibleByDefault(false)
        player.showEntity(m, entity)
    }

    var fakeClaimPos1: Location? = null
    var fakeClaimPos2: Location? = null

    fun launchMainThreadTutorialTask(block: suspend () -> (Unit)): Job {
        val job = m.launchMainThread {
            block()
        }
        activeJobs.add(job)
        job.invokeOnCompletion {
            // Bukkit.broadcastMessage("I have been completed (maybe)")
            activeJobs.remove(job)
        }
        return job
    }

    fun switchActiveTutorial(newActiveTutorial: SparklyTutorial) {
        for (job in activeJobs)
            job.cancel()
        activeTutorial.cleanUp()
        activeJobs.clear()
        launchMainThreadTutorialTask {
            newActiveTutorial.onStart()
        }
    }

    fun remove() {
        for (job in activeJobs)
            job.cancel()
        activeTutorial.cleanUp()
        activeJobs.clear()
        pantufaNPC.remove()
    }

    fun sendMessageAsPantufa(content: TextComponent) {
        player.sendMessage(
            textComponent {
                appendTextComponent {
                    color(NamedTextColor.LIGHT_PURPLE)
                    content("Pantufa")
                }

                appendTextComponent {
                    color(NamedTextColor.GOLD)
                    content(" ➤ ")
                }

                append(content)
            }
        )
    }

    fun sendMessageAsGriefer(content: TextComponent) {
        player.sendMessage(
            textComponent {
                appendTextComponent {
                    color(NamedTextColor.RED)
                    content("Griefer")
                }

                appendTextComponent {
                    color(NamedTextColor.GOLD)
                    content(" ➤ ")
                }

                append(content)
            }
        )
    }
}