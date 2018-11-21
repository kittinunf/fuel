dependencies {
    api(project(":fuel"))
    implementation(Kotlin.stdlib)
    testImplementation(RoboElectric.dependency)
    testImplementation(project(":fuel-json"))
    testImplementation(project(":fuel-test"))
}
