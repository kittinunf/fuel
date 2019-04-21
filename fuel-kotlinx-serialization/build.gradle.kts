plugins {
    id(KotlinX.Serialization.plugin)
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    api(project(":fuel"))

    implementation(KotlinX.Serialization.dependency)

    testImplementation(project(":fuel-test"))
}
