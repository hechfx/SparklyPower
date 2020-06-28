import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(project(":DreamCash"))
    compile(project(":DreamVanish"))
    compileOnly(files("../libs/patched_1.15.1.jar"))
    compile("net.perfectdreams.dreamcore:DreamCore:1.0-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
