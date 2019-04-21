dependencies {
    api(project(Fuel.name))

    implementation(Jackson.dependency)

    testImplementation(project(Fuel.Test.name))
}
