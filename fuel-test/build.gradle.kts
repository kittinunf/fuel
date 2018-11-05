plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(MockServer.dependency)
    compile(Json.dependency)
}
