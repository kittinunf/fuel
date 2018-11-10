plugins { java }

dependencies {
    compile(Result.dependency)
    compile(Stetho.plugin)
    compile(Stetho.StethoUrlConnection.plugin)
    testCompile(Json.dependency)
    testCompile(project(":fuel-test"))
}
