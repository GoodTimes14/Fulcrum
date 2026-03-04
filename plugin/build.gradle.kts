plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`

}



dependencies {
    compileOnly(project(":API"))
    compileOnly(project(":config"))
    compileOnly(project(":database"))

    compileOnly("net.kyori:adventure-api:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.26.1")
    compileOnly(libs.configme)
}
