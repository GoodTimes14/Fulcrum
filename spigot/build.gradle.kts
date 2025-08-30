plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`
}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/nms/") }

}

dependencies {
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    implementation(project(":API"))
    implementation(project(":plugin"))
    implementation(project(":config"))

}
