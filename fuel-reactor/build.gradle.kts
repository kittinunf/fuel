repositories {
    maven(url = "http://repo.spring.io/milestone")
}

dependencies {
    api(project(Fuel.name))

    implementation(Reactor.core)

    testImplementation(project(Fuel.Test.name))
    testImplementation(Jackson.dependency)
    testImplementation(Reactor.test)
}
