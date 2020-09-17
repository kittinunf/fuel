import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Library.KOTLIN_VERSION
    kotlin("kapt") version Library.KOTLIN_VERSION apply false
    id("com.vanniktech.maven.publish") version "0.10.0" apply false
    jacoco
}

val sampleModules = listOf("progress", "simple-client", "weather")

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")

    repositories {
        mavenCentral()
        jcenter()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    val isSample = project.name in sampleModules
    if (!isSample) {
        kotlin {
            // for strict mode
            explicitApi()
        }
    }

    jacoco {
        toolVersion = "0.8.6"
    }

    tasks.jacocoTestReport {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
            csv.isEnabled = false
        }
    }
}
