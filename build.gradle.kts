plugins {
    kotlin("multiplatform") version "1.8.10" apply false
    id("org.jetbrains.kotlinx.kover") version "0.6.1" apply false
}

allprojects {
    apply(plugin = "kover")

    repositories {
        mavenCentral()
    }

    group = "com.github.kittinunf.fuel"
    version = "3.0.0-SNAPSHOT"
}
