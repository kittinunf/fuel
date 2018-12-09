dependencies {
    api(project(":fuel"))
    implementation(Kotlin.stdlib)
    implementation(AndroidX.Arch.extensions)
    testImplementation(RoboElectric.dependency)
    testImplementation(JUnit.dependency)
    testImplementation(project(":fuel-test"))
    testImplementation(Android.Arch.testingCore)
    testImplementation(AndroidX.Test.junit)
}