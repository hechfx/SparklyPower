import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.perfectdreams.net/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    compileOnly(files("../libs/paper_server.jar"))
    compileOnly("io.github.waterfallmc:waterfall-api:1.13-SNAPSHOT")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.4.2")
    api(kotlin("reflect"))
    api(kotlin("script-util"))
    api(kotlin("compiler"))
    api(kotlin("scripting-compiler"))
    api("io.github.microutils:kotlin-logging:1.7.9")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveBaseName.set("KotlinRuntime-shadow")

        exclude {
            it.file?.name?.startsWith("paper_server") == true || it.file?.name?.startsWith("waterfall") == true
        }
    }

    "build" {
        dependsOn(shadowJar)
    }
}