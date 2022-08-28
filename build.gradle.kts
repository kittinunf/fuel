plugins {
    kotlin("multiplatform") version "1.7.10" apply false
    id("org.jetbrains.kotlinx.kover") version "0.6.0" apply false
}

allprojects {
    apply(plugin = "kover")

    repositories {
        mavenCentral()
    }

    group = "com.github.kittinunf.fuel"
    version = "3.0.0-SNAPSHOT"
}
