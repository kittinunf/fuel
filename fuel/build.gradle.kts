plugins { java }

dependencies {
    compile(Result.dependency)
    testCompile(Json.dependency)
    testCompile(MockServer.dependency)
}
