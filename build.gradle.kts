import java.util.Properties
import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.graphql

plugins {
    application
    idea
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.expediagroup.graphql") version "5.3.2"
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
version = "2.0"

dependencies {
    implementation(libs.kotlin.stdlib)
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

java {                                      
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    sourceSets["main"].kotlin.srcDir(generatedSourcesPath)
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
            jvmTarget = "11"
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
}

application {
    mainClass.set("basura.BotKt")
}