plugins {
    id("java")
}

group = "it.raniero"
version = "1.0.0"
description = "swiss knife for minecraft plugin development"

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
    }

    repositories {
        mavenCentral()

        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }


        maven {
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }


        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://repo.onarandombox.com/public")
        }

        maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }

    }

}