dependencies {
    api(project(":fuel"))

    implementation(Gson.dependency)

    testImplementation(project(":fuel-test"))
}
