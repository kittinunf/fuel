plugins {
    kotlin("multiplatform") version "1.5.31" apply false
    jacoco
}

allprojects {
    apply(plugin = "jacoco")

    repositories {
        mavenCentral()
    }

    group = "com.github.kittinunf.fuel"
    version = "3.0.0-SNAPSHOT"

    jacoco {
        toolVersion = "0.8.7"
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.required.set(true)
            csv.required.set(false)
        }
    }
}
