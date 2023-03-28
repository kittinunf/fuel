plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("publication")
}

kotlin {
    explicitApi()
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

    kaptTest(libs.moshi.kotlin.codegen)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
}
