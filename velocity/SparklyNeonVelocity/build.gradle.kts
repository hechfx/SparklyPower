import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("kapt") // Required for Velocity Annotations
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

// This is our DreamCore configuration that has all the dependencies (so plugins can just need to include the project as a project dependency)
//
// The reason we needed to do this is because we want ALL the declared "api" dependencies here BUT we also want the relocated dependencies
// done by the shadow JAR!
//
// So, to do that, we create a configuration that extends the default "shadow" configuration and the "compileClasspath" configuration, this allows us
// to satisfy both of our needs!
val shadowWithRuntimeDependencies by configurations.creating {
    // If you want this configuration to share the same dependencies, otherwise omit this line
    extendsFrom(configurations["shadow"], configurations["compileClasspath"])
}

dependencies {
    implementation(project(":common:rpc-payloads"))
    compileOnlyApi(project(":velocity:SparklyVelocityCore"))
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    compileOnly("net.luckperms:api:5.4")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveBaseName.set("SparklyNeonVelocity-shadow")

        dependencies {
            include {
                it.name == "sparklypower-parent.common:rpc-payloads:unspecified"
            }
        }
    }

    "build" {
        dependsOn(shadowJar)
    }
}