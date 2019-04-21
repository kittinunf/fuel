dependencies {
    api(project(":fuel"))

    implementation(Stetho.plugin)
    implementation(Stetho.StethoUrlConnection.plugin)

    testImplementation(project(":fuel-test"))
}
