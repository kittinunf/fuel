apply(plugin = "com.vanniktech.maven.publish")

plugins {
    kotlin("kapt")
}

dependencies {
    api (project(":fuel-base"))
    api (Library.MOSHI)

    kaptTest(Library.MOSHI_KOTLIN_CODEGEN)

    testImplementation (project(":fuel-singleton"))
    testImplementation (Library.JUNIT)
    testImplementation (Library.OKHTTP_MOCK_WEB_SERVER)
}
