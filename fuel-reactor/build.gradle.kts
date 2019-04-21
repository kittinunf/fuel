repositories {
    maven(url = "http://repo.spring.io/milestone")
}

dependencies {
    api(project(":fuel"))

    implementation(Reactor.core)

    testImplementation(project(":fuel-test"))
    testImplementation(Jackson.dependency)
    testImplementation(Reactor.test)
}
