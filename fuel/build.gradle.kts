plugins { java }

dependencies {
    compile(Result.dependency)
    compile(Json.dependency)
    
    testCompile(Json.dependency)
    testCompile(project(":fuel-test"))
}
