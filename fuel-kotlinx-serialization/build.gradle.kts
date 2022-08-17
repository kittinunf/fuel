plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.7.10"
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
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        // nodejs()
    }
    ios {
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
                api(libs.kotlinx.serialization.json)
                api(libs.result)
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
                implementation(libs.mockwebserver)
            }
        }
        val jsMain by getting
        val jsTest by getting
        val iosMain by getting
        val iosTest by getting
    }
}
