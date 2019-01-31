dependencies {
    api(project(":fuel"))
    implementation(Kotlin.stdlib)
    implementation(Stetho.plugin)
    implementation(Stetho.StethoUrlConnection.plugin)

    testCompile(project(":fuel-test"))
}
