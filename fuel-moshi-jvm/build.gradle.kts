plugins {
    kotlin("jvm")
    id("publication")
    id("org.jetbrains.kotlinx.kover")
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"
}

kotlin {
    explicitApi()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(project(":fuel"))
    api(libs.moshi)
    api(libs.result.jvm)

    kspTest(libs.moshi.kotlin.codegen)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
}
