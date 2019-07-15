dependencies {
    api(project(Fuel.name))

    implementation(Stetho.dependency)
    implementation(Stetho.StethoUrlConnection.dependency)

    testImplementation(project(Fuel.Test.name))
}
