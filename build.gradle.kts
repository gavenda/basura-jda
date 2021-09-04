import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
}

group = "basura"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    // For GraphQL
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // Jackson
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    // Dependency Injection
    implementation("org.kodein.di:kodein-di:7.7.0")

    // Discord API
    implementation("net.dv8tion:JDA:4.3.0_299") {
        exclude("club.minnced")
    }

    // Logging
    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

    // HTML to Markdown
    implementation("io.github.furstenheim:copy_down:1.0")

    // DB
    implementation("org.ktorm:ktorm-core:3.4.1") // ORM
    implementation("com.zaxxer:HikariCP:5.0.0") // Pool
    implementation("org.postgresql:postgresql:42.2.23") // DB
    implementation("org.flywaydb:flyway-core:7.14.1") // Migration
}

val basuraMainClass = "basura.MainKt"

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "16"
            freeCompilerArgs = listOf(
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }

    val fatJar = register<Jar>("fatJar") {
        manifest {
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = archiveVersion
            attributes["Main-Class"] = basuraMainClass
        }

        archiveBaseName.set("${project.name}-bot")

        val dependencyClasses = configurations.runtimeClasspath.get()
            .filter { it.path.endsWith(".jar") }
            .map {
                if (it.isDirectory) it else zipTree(it)
            }

        from(dependencyClasses)
        with(jar.get() as CopySpec)
    }

    build {
        dependsOn(fatJar)
    }
}
