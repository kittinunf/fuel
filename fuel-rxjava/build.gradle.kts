plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.rxJavaJvm)
    testCompile(Dependencies.mockServer)
}
