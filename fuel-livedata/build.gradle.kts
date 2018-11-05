dependencies {
    api(project(":fuel"))
    implementation(Kotlin.stdlib)
    implementation(Android.Arch.extensions)
    testImplementation(RoboElectric.dependency)
    testImplementation(JUnit.dependency)
    testCompile(project(":fuel-test"))
}