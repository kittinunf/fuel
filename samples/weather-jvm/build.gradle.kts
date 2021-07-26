plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")

    implementation(project(":fuel"))
    implementation(project(":fuel-moshi-jvm"))
}