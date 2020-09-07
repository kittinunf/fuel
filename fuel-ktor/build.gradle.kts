apply(plugin = "com.vanniktech.maven.publish")

dependencies {
    implementation(Library.KTOR_CLIENT_JVM)
    implementation(project(":fuel-base"))

    testImplementation(Library.JUNIT)
}
