plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Forge.dependency)
    testCompile(MockServer.dependency)
}
