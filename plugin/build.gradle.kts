plugins {
    fulcrum.`base-conventions`
    fulcrum.`shadow-conventions`

}



dependencies {
    compileOnly(project(":API"))
    compileOnly(project(":config"))
    compileOnly(project(":database"))


    compileOnly(libs.configme)

}
