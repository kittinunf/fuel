import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Library.KOTLIN_VERSION
    kotlin("kapt") version Library.KOTLIN_VERSION apply false
    id("com.vanniktech.maven.publish") version "0.10.0" apply false
    jacoco
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")
    apply(from = "$rootDir/ktlint.gradle.kts")

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    jacoco {
        toolVersion = "0.8.5"
    }

    tasks.jacocoTestReport {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
            csv.isEnabled = false
        }
    }
}
