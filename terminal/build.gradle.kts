plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/nms/") }

}

dependencies {
    compileOnly(libs.spigot)
    implementation(project(":API"))
    implementation(project(":plugin"))
    implementation(project(":database"))
    implementation(project(":config"))

}
