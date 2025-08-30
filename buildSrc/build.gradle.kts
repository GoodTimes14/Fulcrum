plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.0.21"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.lombok)
    implementation(libs.shadow)
    implementation(libs.spotless)
}