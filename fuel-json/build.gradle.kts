plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Json.dependency)
    testCompile(project(":fuel-test"))
}
