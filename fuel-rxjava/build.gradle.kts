dependencies {
    api(project(":fuel"))

    testImplementation(project(":fuel-test"))
    testImplementation(RxJava.Jvm.dependency)
}
