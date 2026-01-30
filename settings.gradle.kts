
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}



rootProject.name = "fulcrum"
include("API")
include("plugin")
include("config")
include("database")
include("terminal")
include("spigot")
include("bungeecord")
include("velocity")
