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
    implementation("com.mysql:mysql-connector-j:9.2.0")
    implementation("net.kyori:adventure-api:4.26.1")
    implementation("net.kyori:adventure-api:4.26.1")

}