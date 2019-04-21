dependencies {
    api(project(":fuel"))

    implementation(RxJava.Jvm.dependency)

    testImplementation(project(":fuel-test"))
}
