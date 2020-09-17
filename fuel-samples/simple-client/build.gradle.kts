plugins {
    kotlin("kapt")
}

dependencies {
    implementation(project(":fuel-singleton"))
    implementation(project(":fuel-moshi"))
    kapt(Library.MOSHI_KOTLIN_CODEGEN)
}
