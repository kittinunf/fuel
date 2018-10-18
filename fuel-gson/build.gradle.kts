plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Gson.dependency)
    testCompile(MockServer.dependency)
}
