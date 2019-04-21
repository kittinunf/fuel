dependencies {
    api(project(":fuel"))

    implementation(Jackson.dependency)

    testImplementation(project(":fuel-test"))
}
