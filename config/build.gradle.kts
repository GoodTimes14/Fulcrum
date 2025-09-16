plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`

}

dependencies {
    implementation(libs.configme)
    compileOnly(project(":API"))
}
