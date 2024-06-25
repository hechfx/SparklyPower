plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    paperweight.devBundle("net.sparklypower.sparklypaper", "1.20.6-R0.1-SNAPSHOT")
    compileOnly(project(":bukkit:DreamCore"))
    compileOnly(project(":bukkit:DreamMini"))
    compileOnly(project(":bukkit:DreamBedrockIntegrations"))
    compileOnly(files("../../libs/mcMMO.jar"))
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    reobfJar {
        // For some reason the userdev plugin is using "unspecified" as the suffix, and that's not a good name
        // So we are going to change it to "PluginName-reobf.jar"
        outputJar.set(layout.buildDirectory.file("libs/${project.name}-reobf.jar"))
    }

    // Configure reobfJar to run when invoking the build task
    build {
        dependsOn(reobfJar)
    }
}