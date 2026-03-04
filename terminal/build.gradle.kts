plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
}

dependencies {
    implementation(project(":API"))
    implementation(project(":plugin"))
    implementation(project(":database"))
    implementation(project(":config"))
    implementation("net.kyori:adventure-api:4.26.1")

}
