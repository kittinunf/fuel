dependencies {
    api(project(":fuel"))

    testImplementation(RoboElectric.dependency)
    testImplementation(project(":fuel-json"))
    testImplementation(project(":fuel-test"))
}
