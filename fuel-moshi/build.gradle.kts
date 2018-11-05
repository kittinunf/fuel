plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":fuel"))
    implementation(Moshi.dependency)
    testCompile(project(":fuel-test"))
}
