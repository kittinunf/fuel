plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.21"
    id("publication")
    id("org.jetbrains.kotlinx.kover")
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

        val appleMain by creating {
            dependsOn(commonMain)
        }

        val iosMain by getting {
            dependsOn(appleMain)
        }
        val macosArm64Main by getting {
            dependsOn(appleMain)
        }
        val macosX64Main by getting {
            dependsOn(appleMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(appleMain)
        }

        val appleTest by creating {
            dependsOn(commonTest)
        }

        val iosTest by getting {
            dependsOn(appleTest)
        }
        val macosArm64Test by getting {
            dependsOn(appleTest)
        }
        val macosX64Test by getting {
            dependsOn(appleTest)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(appleTest)
        }
    }
}
