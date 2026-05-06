import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
    fulcrum.`publish-conventions`
}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/nms/") }

}

dependencies {
    compileOnly(libs.spigot)
    compileOnly(libs.configme)
    implementation("net.kyori:adventure-platform-bukkit:4.4.1")
    implementation(project(":API"))
    implementation(project(":plugin"))
    implementation(project(":database"))
    implementation(project(":config"))

}
