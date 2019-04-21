dependencies {
    api(project(Fuel.name))

    implementation(AndroidX.Arch.extensions)

    testImplementation(project(Fuel.Test.name))
    testImplementation(Android.Arch.testingCore)
    testImplementation(AndroidX.Test.junit)
    testImplementation(RoboElectric.dependency)
}
