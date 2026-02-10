import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
    fulcrum.`publish-conventions`
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    implementation(project(":API"))
    implementation(project(":plugin"))
    implementation(project(":database"))
    implementation(project(":config"))
}

publishing.publications.create<MavenPublication>("maven") {

    artifactId = "fulcrum-" + project.name
    version = correctVersion(rootProject.version.toString() + (if (isSnapshot()) "-" + getGitBranch() else ""))
    group = rootProject.group.toString()

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
