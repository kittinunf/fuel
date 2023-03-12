plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.10"
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
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
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
    macosArm64 {
        binaries {
            framework {
                baseName = "Fuel-Serialization"
            }
        }
    }
    macosX64 {
        binaries {
            framework {
                baseName = "Fuel-Serialization"
            }
        }
    }
    iosSimulatorArm64 {
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
