plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "fuel.jackson"

kotlin {
    explicitApi()
}

dependencies {
    api(project(":fuel-singleton"))
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")
}
