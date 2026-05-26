import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
    fulcrum.`publish-conventions`
}



dependencies {
    compileOnly(libs.configme)
    compileOnly(libs.lettuce)
    compileOnly("net.kyori:adventure-api:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.26.1")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed", "skipped")
        showStandardStreams = false
    }
}
