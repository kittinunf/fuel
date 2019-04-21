dependencies {
    api(project(Fuel.name))

    implementation(Stetho.plugin)
    implementation(Stetho.StethoUrlConnection.plugin)

    testImplementation(project(Fuel.Test.name))
}
