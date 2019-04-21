dependencies {
    api(project(":fuel"))

    implementation(Moshi.dependency)

    testImplementation(project(":fuel-test"))
}
