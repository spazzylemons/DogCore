import org.jooq.meta.jaxb.Property

plugins {
    kotlin("jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.4.0"
    id("nu.studer.jooq") version "8.2"
}

group = "net.dumbdogdiner"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    // commands
    compileOnly("com.mojang:brigadier:1.0.18")
    implementation("com.github.Revxrsal.Lamp:common:3.1.5")
    implementation("com.github.Revxrsal.Lamp:bukkit:3.1.5")

    // database
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("org.jooq:jooq:3.18.4")

    // permissions
    compileOnly("net.luckperms:api:5.4")

    jooqGenerator("org.jooq:jooq-meta-extensions:3.18.4")
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveClassifier.set("")
    }

    compileKotlin {
        kotlinOptions {
            // preserve parameter names, so that the command reflection can use them
            javaParameters = true
        }
    }
}

kotlin {
    jvmToolchain(17)
}

jooq {
    version.set("3.18.4")

    configurations {
        create("main") {
            jooqConfiguration.apply {
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        properties.apply {
                            add(
                                Property().apply {
                                    key = "scripts"
                                    value = "src/main/resources/database.sql"
                                }
                            )
                        }
                    }
                    target.apply {
                        packageName = "net.dumbdogdiner.dogcore.database.schema"
                    }
                }
            }
        }
    }
}
