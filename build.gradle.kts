plugins {
    id("net.neoforged.moddev") version "2.0.138"
    id("com.almostreliable.almostgradle") version "2.1.1"
    id("idea")
}

val rhinoVersion: String by project

almostgradle.setup {
    javaVersion = 25
    buildConfig = false
    dataGen = "src/generated/resources"
    splitRunDirs = true
}

neoForge {
    accessTransformers {
        publish(file("src/main/resources/META-INF/accesstransformer.cfg"))
    }
}

repositories {
    mavenCentral()
    maven {
        name = "Latvian Maven"
        url = uri("https://maven.latvian.dev/releases")
        content {
            includeGroup("dev.latvian.mods")
            includeGroup("dev.latvian.apps")
        }
    }
    maven {
        name = "Curse Maven"
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    api("dev.latvian.mods:rhino:$rhinoVersion") {
        isTransitive = false
    }

    api("dev.latvian.mods:KubeJS-26.1.2") {
        version {
            branch = "2601"
        }
        isTransitive = false
    }

    api("curse.maven:geckolib-388172:7913641")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven("file://${project.projectDir}/repo")
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "5000"))
}