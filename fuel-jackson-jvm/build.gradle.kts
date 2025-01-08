import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("publication")
    alias(libs.plugins.kover)
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

dependencies {
    api(project(":fuel"))
    api(libs.jackson.module.kotlin)
    api(libs.result.jvm)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
}
