dependencies {
    api(project(":fuel"))
    implementation(Dependencies.kotlinStdlib)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.junit)
    testCompile(Dependencies.mockServer)
}
