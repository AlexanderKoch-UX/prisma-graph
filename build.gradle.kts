plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "com.alexanderkoch.prismagraph"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("junit:junit:4.13.2")
}

intellij {
    version.set("2024.3.1")
    type.set("IC") // IntelliJ Community - compatible with WebStorm
    plugins.set(listOf())
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("252.*")
        
        // Plugin-Beschreibung aktualisieren
        changeNotes.set("""
            Version 1.0.0:
            - Kompatibilität mit WebStorm 2025.1 (Build 251.*)
            - Erste Version des Prisma Graph Visualizers
            - Grundlegende Schema-Parsing-Funktionalität
            - Graphische Darstellung von Models und Relationen
        """.trimIndent())
    }
}