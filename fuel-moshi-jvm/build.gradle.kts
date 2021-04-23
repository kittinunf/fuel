plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "fuel.moshi"

kotlin {
    explicitApi()
}

dependencies {
    api (project(":fuel-singleton"))
    api ("com.squareup.moshi:moshi:1.12.0")
}
