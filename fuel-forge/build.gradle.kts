apply(plugin = "com.vanniktech.maven.publish")

dependencies {
    api (project(":fuel-base"))
    api (Library.FORGE)

    testImplementation (project(":fuel-default"))
    testImplementation (Library.JUNIT)
    testImplementation (Library.OKHTTP_MOCK_WEB_SERVER)
}
