dependencies {
    api(project(":fuel"))
    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.androidArchExtensions)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.junit)
    testCompile(Dependencies.mockServer)
}