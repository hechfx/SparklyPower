pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}


rootProject.name = "sparklypower-parent"

// ===[ GENERAL PURPOSE ]===
include(":common-utils")
include(":common:KotlinRuntime")
include(":common:tables")
include(":common:rpc-payloads")

// ===[ PAPER ]===
include(":bukkit:DreamAntiAFK")
include(":bukkit:DreamCore")
include(":bukkit:DreamAuth")
include(":bukkit:DreamCash")
include(":bukkit:DreamVote")
include(":bukkit:DreamQuiz")
include(":bukkit:DreamCorrida")
include(":bukkit:DreamChat")
// include(":bukkit:DreamChallenges")
include(":bukkit:DreamMoverSpawners")
include(":bukkit:DreamLoja")
include(":bukkit:DreamCaixaSecreta")
include(":bukkit:DreamJetpack")
include(":bukkit:DreamMochilas")
include(":bukkit:DreamEnchant")
include(":bukkit:DreamTrails")
include(":bukkit:DreamRaspadinha")
include(":bukkit:DreamHome")
include(":bukkit:DreamMini")
include(":bukkit:DreamResourcePack")
include(":bukkit:DreamCasamentos")
include(":bukkit:DreamClubes")
include(":bukkit:DreamScoreboard")
include(":bukkit:DreamTorreDaMorte")
include(":bukkit:DreamRestarter")
include(":bukkit:DreamVanish")
// include(":bukkit:DreamLobbyFun")
include(":bukkit:DreamMinaRecheada")
include(":bukkit:DreamBusca")
include(":bukkit:DreamAssinaturas")
include(":bukkit:DreamBlockVIPItems")
include(":bukkit:DreamWarps")
include(":bukkit:DreamHeads")
include(":bukkit:DreamTerrainAdditions")
include(":bukkit:DreamLabirinto")
include(":bukkit:DreamFight")
include(":bukkit:DreamQuickHarvest")
include(":bukkit:DreamPicaretaMonstra")
include(":bukkit:DreamBroadcast")
include(":bukkit:DreamMobSpawner")
include(":bukkit:DreamElevador")
include(":bukkit:DreamCassino")
include(":bukkit:DreamReparar")
include(":bukkit:DreamMusically")
include(":bukkit:DreamTreeAssist")
include(":bukkit:DreamFusca")
include(":bukkit:DreamBedrockIntegrations")
include(":bukkit:DreamBrisa")
include(":bukkit:DreamVIPStuff")
include(":bukkit:DreamChatTags")
include(":bukkit:DreamResourceReset")
include(":bukkit:DreamKits")
include(":bukkit:DreamMcMMOFun")
include(":bukkit:DreamShopHeads")
// include(":bukkit:DreamRedstoneClockDetector")
include(":bukkit:DreamChestShopStuff")
include(":bukkit:DreamLagStuffRestrictor")
include(":bukkit:DreamEsponjas")
include(":bukkit:DreamArmorStandEditor")
include(":bukkit:DreamPrivada")
include(":bukkit:DreamDropParty")
include(":bukkit:DreamMapWatermarker")
include(":bukkit:DreamDemocracy")
include(":bukkit:DreamDiscordCommandRelayer")
include(":bukkit:DreamColorEmote")
include(":bukkit:DreamLobbyFun")
include(":bukkit:DreamCustomItems")
include(":bukkit:DreamPvPTweaks")
include(":bukkit:DreamRoadProtector")
include(":bukkit:DreamEmptyWorldGenerator")
include(":bukkit:DreamCorreios")
include(":bukkit:SparklyDreamer")
include(":bukkit:DreamXizum")
include(":bukkit:DreamRaffle")
// include(":bukkit:DreamNews")
include(":bukkit:SparklyChunkLoader")
include(":bukkit:DreamTNTRun")
include(":bukkit:DreamResourceGenerator")
include(":bukkit:DreamAjuda")
include(":bukkit:DreamEnderHopper")
include(":bukkit:DreamSplegg")
include(":bukkit:DreamSocial")
include(":bukkit:DreamSonecas")
include(":bukkit:DreamSeamlessWorlds")
include(":bukkit:DreamInventorySnapshots")
include(":bukkit:DreamBlockParty")
include(":bukkit:DreamHolograms")

// ===[ VELOCITY ]===
include(":velocity:SparklyNeonVelocity")

// ===[ PANTUFA ]===
include(":pantufa")