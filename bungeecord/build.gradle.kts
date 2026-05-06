import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
    fulcrum.`publish-conventions`

}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/nms/") }

}

dependencies {
    compileOnly(libs.bungeecord)
    compileOnly(libs.configme)
    implementation(project(":API"))
    implementation(project(":plugin"))
    implementation(project(":database"))
    implementation(project(":config"))

    implementation("net.kyori:adventure-platform-bungeecord:4.4.1")

}