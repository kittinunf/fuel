plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":fuel"))
    implementation(Moshi.dependency)
    testCompile(MockServer.dependency)
}
