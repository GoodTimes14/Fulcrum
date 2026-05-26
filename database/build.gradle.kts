import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
    fulcrum.`publish-conventions`
}


dependencies {
    implementation(project(":API"))
    implementation(libs.lettuce)
    implementation(libs.hikaricp)
    implementation("org.apache.commons:commons-pool2:2.12.0")
    implementation("com.mysql:mysql-connector-j:9.3.0")
    implementation("net.kyori:adventure-api:4.26.1")
    implementation("net.kyori:adventure-api:4.26.1")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.4"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")

    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.awaitility:awaitility:4.2.2")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.16")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed", "skipped")
        showStandardStreams = false
    }
    // Force a modern Docker API version so docker-java (bundled with Testcontainers)
    // negotiates correctly with newer daemons that have dropped pre-1.40 support.
    environment("DOCKER_API_VERSION", "1.43")

    // Pass -PskipDockerTests to disable Testcontainers-backed tests (lock tests still run).
    val skipDocker = providers.gradleProperty("skipDockerTests").isPresent
            || System.getenv("FULCRUM_SKIP_DOCKER_TESTS") == "true"
    systemProperty("fulcrum.skipDockerTests", skipDocker.toString())
}
