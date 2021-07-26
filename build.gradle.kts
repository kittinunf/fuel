plugins {
    kotlin("multiplatform") version "1.5.30-M1" apply false
    jacoco
}

allprojects {
    apply(plugin = "jacoco")

    repositories {
        jcenter()
        mavenCentral()
    }

    group = "com.github.kittinunf.fuel"
    version = "3.0.0-SNAPSHOT"
}
