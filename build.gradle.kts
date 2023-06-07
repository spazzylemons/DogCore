plugins {
    kotlin("jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.4.0"
}

group = "net.dumbdogdiner"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly("dev.jorel:commandapi-bukkit-core:9.0.1")
    implementation("dev.jorel:commandapi-bukkit-kotlin:9.0.1")

    implementation("org.postgresql:postgresql:42.5.4")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveClassifier.set("")
    }
}

kotlin {
    jvmToolchain(17)
}
