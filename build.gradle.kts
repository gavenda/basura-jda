import java.util.Properties

plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

group = "basura"
version = "1.1-SNAPSHOT"

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")

    // For GraphQL
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Jackson
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    // Dependency Injection
    implementation("org.kodein.di:kodein-di:7.10.0")

    // Discord API
    implementation("net.dv8tion:JDA:5.0.0-alpha.4") {
        exclude("club.minnced")
    }

    // Logging
    implementation("org.apache.logging.log4j:log4j-api:2.17.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.0")

    // HTML to Markdown
    implementation("io.github.furstenheim:copy_down:1.0")

    // DB
    implementation("org.ktorm:ktorm-core:3.4.1") // ORM
    implementation("com.zaxxer:HikariCP:5.0.0") // Pool
    implementation("org.postgresql:postgresql:42.3.1") // DB
    implementation("org.flywaydb:flyway-core:8.3.0") // Migration
}

java {                                      
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf(
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=kotlinx.coroutines.DelicateCoroutinesApi",
                "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi"
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
    mainClass.set( "basura.MainKt")
}