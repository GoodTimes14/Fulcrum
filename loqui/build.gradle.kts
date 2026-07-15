import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
    fulcrum.`publish-conventions`
}



dependencies {

    implementation(libs.jackson)
    implementation(libs.lettuce)
    implementation(project(":API"))
    implementation(project(":config"))
    implementation(project(":database"))
}
