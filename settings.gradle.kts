plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

sourceControl {
    gitRepository(uri("https://github.com/KubeJS-Mods/KubeJS.git")) {
        producesModule("dev.latvian.mods:KubeJS-26.1.2")
    }
}

val modName: String by extra
val minecraftVersion: String by extra
rootProject.name = "$modName-$minecraftVersion"
