package net.perfectdreams.dreamajuda.cutscenes

import kotlinx.serialization.json.Json
import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamajuda.theatermagic.TheaterMagicStoredRecordingAnimation
import java.io.File

class CutsceneCache(val m: DreamAjuda) {
    val lorittaSpeaks = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "loritta_speaks.json").readText()
    )

    val leavingHouse = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "leaving_house.json").readText()
    )

    val mining = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "mining.json").readText()
    )

    val build1 = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "build1.json").readText()
    )

    val build2 = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "build2.json").readText()
    )

    val build3 = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "build3.json").readText()
    )

    val build4 = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "build4.json").readText()
    )

    val pantufaProtection = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "pantufa_protection.json").readText()
    )

    val pantufaProtectionIntro = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "pantufa_protection_intro.json").readText()
    )

    val tutorialGrieferIntro = Json.decodeFromString<TheaterMagicStoredRecordingAnimation>(
        File(m.dataFolder, "tutorial_griefer_intro.json").readText()
    )
}