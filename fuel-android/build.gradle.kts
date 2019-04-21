dependencies {
    api(project(":fuel"))

    testImplementation(project(":fuel-test"))
    testImplementation(RoboElectric.dependency)
    testImplementation(project(":fuel-json"))
}
