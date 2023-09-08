import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    val arrowVersion = "2.0.0-SNAPSHOT"
    val xef = "0.0.4-alpha.11.1+368b069"

    implementation(compose.desktop.currentOs)

    implementation("media.kamel:kamel-image:0.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.xebia:xef-kotlin:$xef")
    implementation("com.xebia:xef-reasoning:$xef")
    implementation("ch.qos.logback:logback-classic:1.4.8")
    implementation ("com.aallam.openai:openai-client:3.3.2")
    implementation ("io.ktor:ktor-client-android:2.2.4")

    implementation("io.arrow-kt:arrow-optics:$arrowVersion")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:$arrowVersion")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.2")
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main")
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        freeCompilerArgs.add("-Xcontext-receivers")
        freeCompilerArgs.add("-Xenable-builder-inference")
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TestOptics"
            packageVersion = "1.0.0"
        }
    }
}
