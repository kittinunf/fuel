plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "fuel.forge"

kotlin {
    explicitApi()
}

dependencies {
    api (project(":fuel-singleton"))
    api ("com.github.kittinunf.forge:forge:1.0.0-alpha3")
    api ("com.github.kittinunf.result:result:3.1.0")
}
