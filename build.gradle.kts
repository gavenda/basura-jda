import java.util.Properties

plugins {
    application
    idea
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.hidetake.ssh") version "2.10.1"
}

repositories {
    google()
    mavenCentral()

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }

    maven {
        name = "Dv8tion Releases"
        url = uri("https://m2.dv8tion.net/releases")
    }

    maven {
        name = "DRSchlaubi Releases"
        url = uri("https://schlaubi.jfrog.io/artifactory/lavakord")
    }
}

group = "basura"
version = "2.1"

dependencies {
    implementation(libs.kotlin.stdlib)

    // Framework
    implementation(libs.kord.extensions)

    // HTML to Markdown
    implementation("io.github.furstenheim:copy_down:1.0")

    // Logging dependencies
    implementation(libs.groovy)
    implementation(libs.logback)
    implementation(libs.logging)

    // DB
    implementation(libs.ktorm) // ORM
    implementation(libs.ktorm.postgresql) // ORM
    implementation(libs.hikari) // Pool
    implementation(libs.postgresql) // DB
    implementation(libs.flyway) // Migration
}

val generatedSourcesPath = file("build/generated")

kotlin {
    sourceSets["main"].kotlin.srcDir(generatedSourcesPath)
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

idea {
    module {
        generatedSourceDirs.add(generatedSourcesPath)
    }
}

apply(from = "ssh.gradle")

tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=dev.kord.gateway.PrivilegedIntent",
                "-Xopt-in=org.koin.core.annotation.KoinReflectAPI"
            )
        }
    }

    val generateVersionProperties = register(name = "generateVersionProperties") {
        val resourcesDir = File("$buildDir/resources/main").apply {
            mkdirs()
        }
        val propertiesFilePath = File(resourcesDir, "/version.properties").apply {
            createNewFile()
        }
        Properties().apply {
            setProperty("version", version.toString())
            store(propertiesFilePath.outputStream(), null)
        }
    }

    classes.configure {
        dependsOn(generateVersionProperties)
    }

    getByName("deploy") {
        dependsOn(getByName("installDist"))
    }
}

application {
    mainClass.set("basura.BotKt")
}