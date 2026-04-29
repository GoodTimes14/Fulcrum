plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
    application
}

dependencies {
    implementation(project(":API"))
    implementation(project(":plugin"))
    implementation(project(":database"))
    implementation(project(":config"))
    compileOnly(libs.configme)

    implementation("net.kyori:adventure-api:4.26.1")
    implementation("net.kyori:adventure-text-serializer-legacy:4.26.1")
    implementation("net.kyori:adventure-text-serializer-ansi:4.26.1")
    implementation("org.jline:jline-reader:3.30.6")
    implementation("org.jline:jline-terminal:3.30.6")
}

application {
    mainClass.set("it.raniero.fulcrum.terminal.FulcrumTerminalApplication")
}
