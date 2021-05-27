plugins {
    id(Kotlinx.Serialization.plugin)
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    api(project(Fuel.name))

    implementation(Kotlinx.Serialization.Json.dependency)

    testImplementation(project(Fuel.Test.name))
}
