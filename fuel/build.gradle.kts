dependencies {
    api(Result.dependency)
    implementation(ICU.dependency)

    testImplementation(project(Fuel.Test.name))
    testImplementation(Json.dependency)
}
