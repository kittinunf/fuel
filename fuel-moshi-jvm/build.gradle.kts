plugins {
    kotlin("jvm")
    kotlin("kapt")
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
    api("com.squareup.moshi:moshi:1.12.0")

    kaptTest("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")

    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.2")
}