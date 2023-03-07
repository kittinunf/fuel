plugins {
    kotlin("multiplatform") version "1.8.10" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }

    group = "com.github.kittinunf.fuel"
    version = "3.0.0-SNAPSHOT"
}
