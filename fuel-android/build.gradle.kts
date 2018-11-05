dependencies {
    api(project(":fuel"))
    implementation(Kotlin.stdlib)
    testImplementation(RoboElectric.dependency)
    testImplementation(JUnit.dependency)
    testCompile(project(":fuel-test"))
}
