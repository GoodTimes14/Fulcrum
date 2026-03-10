import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("com.gradleup.shadow")
}


tasks.named<ShadowJar>("shadowJar") {

    //minimize()
    archiveFileName.set("${rootProject.name}-${project.name}-${rootProject.version}.jar")
    archiveClassifier.set("")

    relocate("com.zaxxer", "it.raniero.fulcrum.libs.com.zaxxer")
    relocate("com.mysql", "it.raniero.fulcrum.libs.com.mysql")
    relocate("com.google.protobuf", "it.raniero.fulcrum.libs.com.google.protobuf")
    relocate("google.protobuf", "it.raniero.fulcrum.libs.google.protobuf")
    relocate("redis", "it.raniero.fulcrum.libs.redis")


    relocate("org.mariadb", "it.raniero.fulcrum.libs.org.mariadb")
    relocate("io.lettuce", "it.raniero.fulcrum.libs.io.lettuce")
    relocate("io.netty", "it.raniero.fulcrum.libs.io.netty")
    relocate("org.apache", "it.raniero.fulcrum.libs.org.apache")
    relocate("org.slf4j", "it.raniero.fulcrum.libs.org.slf4j")
    relocate("org.reactivestreams", "it.raniero.fulcrum.libs.org.reactivestreams")
    relocate("reactor", "it.raniero.fulcrum.libs.reactor")
    relocate("ch.jalu", "it.raniero.fulcrum.libs.ch.jalu")
    relocate("org.yaml", "it.raniero.fulcrum.libs.org.yaml")
    if(project.name.contains("spigot") || project.name.equals("terminal") || project.name.contains("bungeecord")) {
        relocate("net.kyori", "it.raniero.fulcrum.libs.net.kyori")
    }

    relocate("reactor", "it.raniero.fulcrum.libs.reactor")


    mergeServiceFiles()
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}
