plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "com.alexanderkoch.prismagraph"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
    
    intellijPlatform {
        intellijIdeaCommunity("2025.1.3")
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = false
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }

    patchPluginXml {
        sinceBuild.set("251")
        untilBuild.set("253.*")
        
        // Plugin-Beschreibung aktualisieren
        changeNotes.set("""
            Version 1.0.0:
            - Kompatibilität mit IntelliJ IDEA 2025.1.3 und höher (Build 251.*)
            - Erste Version des Prisma Graph Visualizers
            - Grundlegende Schema-Parsing-Funktionalität
            - Graphische Darstellung von Models und Relationen
        """.trimIndent())
    }
}