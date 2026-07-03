@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")

plugins {
    id("java-library")
    id("xyz.jpenilla.run-velocity") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // API de Velocity
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    // IMPORTANTE: Procesador de anotaciones para que @Plugin funcione y genere los metadatos
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    // API de Sponge Configurate para leer y crear el config.yml (Usamos compileOnly porque Velocity ya lo tiene)
    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    runVelocity {
        velocityVersion("3.5.0-SNAPSHOT")
    }

    processResources {
        val props = mapOf("version" to project.version)
        filesMatching("velocity-plugin.json") {
            expand(props)
        }
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}