plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.5.30-M1"
    `maven-publish`
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    iosX64 {
        binaries {
            framework {
                baseName = "Fuel-Serialization"
            }
        }
    }
    explicitApi()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":fuel"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.2")
            }
        }
        val jsMain by getting
        val jsTest by getting
        val iosX64Main by getting
        val iosX64Test by getting
    }
}
