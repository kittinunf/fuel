apply(plugin = "com.vanniktech.maven.publish")

dependencies {
    api(project(":fuel-base"))
    api(Library.FORGE)
    api(Library.RESULT)

    testImplementation(project(":fuel-singleton"))
    testImplementation(Library.JUNIT)
    testImplementation(Library.OKHTTP_MOCK_WEB_SERVER)
}
