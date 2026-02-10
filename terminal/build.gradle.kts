plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
}

dependencies {
    implementation(project(":API"))
    implementation(project(":plugin"))
    implementation(project(":database"))
    implementation(project(":config"))

}
