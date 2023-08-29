pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("jvm") version "1.9.0"
        id("com.google.devtools.ksp").version("1.9.0-1.0.12")
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
        kotlin("plugin.serialization") version "1.9.0"
    }
}

rootProject.name = "TestOptics"
