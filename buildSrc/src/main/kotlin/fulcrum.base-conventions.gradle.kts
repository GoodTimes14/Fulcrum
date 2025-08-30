plugins {
    `java-library`
    id("io.freefair.lombok")
    id("com.diffplug.spotless")
}

group = rootProject.group
version = rootProject.version
description = rootProject.description

// Java compilation settings
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    disableAutoTargetJvm()
    withSourcesJar()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
}

spotless {
    java {
        endWithNewline()
        leadingSpacesToTabs(4)
        removeUnusedImports()
        trimTrailingWhitespace()
        palantirJavaFormat()
        targetExclude("build/generated/**/*")
    }

    kotlinGradle {
        endWithNewline()
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
    }
}


tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    withType<Jar>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching(listOf("plugin.yml")) {
            expand("version" to ("\"${rootProject.version}\""))
        }
    }


    build {
        // Ensure spotlessApply runs before build
        dependsOn(tasks.named("spotlessApply"))
    }
}

defaultTasks("build")
