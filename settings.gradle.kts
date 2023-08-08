pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("jvm").version(extra["kotlin.version"] as String)
        id("com.google.devtools.ksp").version("1.9.0-1.0.12")
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}

rootProject.name = "TestOptics"
