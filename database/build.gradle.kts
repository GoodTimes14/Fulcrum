plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`

}


dependencies {
    compileOnly(project(":API"))
    implementation(libs.lettuce)
    implementation(libs.hikaricp)
    implementation("org.apache.commons:commons-pool2:2.12.0")
    implementation("com.mysql:mysql-connector-j:9.2.0")
}
