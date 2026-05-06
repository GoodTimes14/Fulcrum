import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
    fulcrum.`publish-conventions`
}

dependencies {
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)

    implementation(libs.configme)
    implementation(project(":API"))
    implementation(project(":plugin"))
    implementation(project(":database"))
    implementation(project(":config"))
}