plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.gson)
    testCompile(Dependencies.mockServer)
}
