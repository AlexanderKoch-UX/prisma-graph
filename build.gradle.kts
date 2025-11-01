plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "com.alexanderkoch.prismagraph"
version = "1.0.1"

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
            Version 1.0.1:
            - Fixed missing Prisma icon - now displays properly in file lists
            - Fixed Mac M1/MacOS touchpad zoom behavior with event throttling
            - Fixed model dragging - can now drag to upper left corner and all edges
            - Added SVG icon support with PNG fallback
            
            Version 1.0.0:
            - Jetbrains IntelliJ IDEA 2025.1.3 Compatibility (Until Build 253.*)
            - Initial Release
        """.trimIndent())
    }
}