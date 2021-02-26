apply(plugin = "com.vanniktech.maven.publish")

plugins {
    kotlin("plugin.serialization") version "1.4.31"
}

val okhttpVersion: String by extra

dependencies {
    api(project(":fuel-base"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    testImplementation(project(":fuel-singleton"))
    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}
