plugins {
	kotlin("jvm")
}

dependencies {
    implementation(project(":fuel"))
    implementation(Dependencies.moshi)
    testCompile(Dependencies.mockServer)
}
