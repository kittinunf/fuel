plugins {
    kotlin("kapt")
}

dependencies {
    implementation (project(":fuel-default"))
    implementation (project(":fuel-moshi"))
    kapt (Library.MOSHI_KOTLIN_CODEGEN)
}
