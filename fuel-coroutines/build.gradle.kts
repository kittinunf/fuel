dependencies {
    api(project(":fuel"))

    implementation(KotlinX.Coroutines.jvm)

    testImplementation(project(":fuel-test"))
}
