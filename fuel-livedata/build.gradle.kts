dependencies {
    api(project(":fuel"))

    implementation(AndroidX.Arch.extensions)

    testImplementation(project(":fuel-test"))
    testImplementation(Android.Arch.testingCore)
    testImplementation(AndroidX.Test.junit)
    testImplementation(RoboElectric.dependency)
}
