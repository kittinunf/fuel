plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(RxJava.Jvm.dependency)
    testCompile(MockServer.dependency)
}
