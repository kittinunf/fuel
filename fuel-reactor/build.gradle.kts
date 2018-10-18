repositories {
    maven(url = "http://repo.spring.io/milestone")
}

dependencies {
    compile(project(":fuel"))
    compile(Reactor.core)
    testCompile(Reactor.test)
    testCompile(MockServer.dependency)
    testCompile(project(":fuel-jackson"))
}
