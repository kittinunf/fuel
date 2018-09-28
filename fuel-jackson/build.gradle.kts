plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.jackson)
    testCompile(Dependencies.mockServer)
}
