plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Jackson.dependency)
    testCompile(MockServer.dependency)
}
