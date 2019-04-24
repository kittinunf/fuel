dependencies {
    api(project(Fuel.name))

    implementation(RxJava.Jvm.dependency)

    testImplementation(project(Fuel.Test.name))
}
