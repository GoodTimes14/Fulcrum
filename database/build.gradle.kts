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
}


publishing.publications.create<MavenPublication>("maven") {

    artifactId = "fulcrum-" + project.name
    version = correctVersion(rootProject.version.toString() + (if (isSnapshot()) "-" + getGitBranch() else ""))
    group = rootProject.group.toString()

    artifact(tasks.named("sourcesJar"))
    artifact(tasks.named<ShadowJar>("shadowJar"))
}


publishing.repositories {
    maven {

        url = uri(correctPublishUrl(findProperty("NEXUS_URL") as String))
        credentials {
            username = (findProperty("NEXUS_REPO_USERNAME") ?: "") as String
            password = (findProperty("NEXUS_REPO_PASSWORD") ?: "") as String
        }
    }
}
