plugins {
    kotlin("kapt")
}

dependencies {
    api(project(Fuel.name))

    implementation(Moshi.dependency)

    testImplementation(project(Fuel.Test.name))
    kaptTest(Moshi.codegen)
}
