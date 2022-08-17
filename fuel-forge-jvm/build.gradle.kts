plugins {
    kotlin("jvm")
    `maven-publish`
}

kotlin {
    explicitApi()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "fuel-forge-jvm"
            from(components["java"])
        }
    }
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
    api(libs.forge)
    api(libs.result.jvm)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
}
