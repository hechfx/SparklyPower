plugins {
    kotlin("jvm")
    `java-library`
    id("com.gradleup.shadow") version "9.0.0-beta4"
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://papermc.io/repo/repository/maven-public/")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnlyApi(project(":velocity:SparklyVelocityCore"))

    compileOnlyApi("com.velocitypowered:velocity-api:3.4.0-sparklyvelocity-SNAPSHOT")
    compileOnlyApi("com.velocitypowered:velocity-proxy:3.4.0-sparklyvelocity-SNAPSHOT")
}