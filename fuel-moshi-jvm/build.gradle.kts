import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("publication")
    id("org.jetbrains.kotlinx.kover")
    id("com.google.devtools.ksp") version "2.0.20-1.0.25"
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
    api(libs.moshi)
    api(libs.result.jvm)

    kspTest(libs.moshi.kotlin.codegen)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
}
