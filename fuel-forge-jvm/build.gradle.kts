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
    api("com.github.kittinunf.forge:forge:1.0.0-alpha3")
    api("com.github.kittinunf.result:result-jvm:5.2.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.2")
}
