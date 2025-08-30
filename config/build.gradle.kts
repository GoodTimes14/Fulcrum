plugins {
    fulcrum.`base-conventions`
}

dependencies {
    implementation("ch.jalu:configme:1.4.1")

    compileOnly(project(":API"))
}
