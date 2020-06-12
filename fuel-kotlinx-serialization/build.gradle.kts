apply(plugin = "com.vanniktech.maven.publish")

plugins {
    kotlin("plugin.serialization") version Library.KOTLIN_VERSION
}

dependencies {
    api (project(":fuel-base"))
    api (Library.KOTLINX_SERIALIZATION_RUNTIME)

    testImplementation (project(":fuel-default"))
    testImplementation (Library.JUNIT)
    testImplementation (Library.OKHTTP_MOCK_WEB_SERVER)
}
