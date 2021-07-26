plugins {
    kotlin("jvm")
    `maven-publish`
}

kotlin {
    explicitApi()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(project(":fuel"))
    api("com.github.kittinunf.forge:forge:1.0.0-alpha3")
    api("com.github.kittinunf.result:result:3.1.0")

    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.2")
}
