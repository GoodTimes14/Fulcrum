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
