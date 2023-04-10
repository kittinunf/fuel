import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform")
    id("publication")
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
                baseName = "Fuel"
            }
        }
    }
    macosArm64 {
        binaries {
            framework {
                baseName = "Fuel"
            }
        }
    }
    macosX64 {
        binaries {
            framework {
                baseName = "Fuel"
            }
        }
    }
    iosSimulatorArm64 {
        binaries {
            framework {
                baseName = "Fuel"
            }
        }
    }

    explicitApi()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.okhttp)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.mockwebserver)
            }
        }

        val jsMain by getting {
            /*dependencies {
                api(npm("node-fetch", "2.6.1"))
                api(npm("abort-controller", "3.0.0"))
            }*/
        }
        val jsTest by getting

        val appleMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.okio)
            }
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
        val iosTest by getting
    }

    kotlin.targets.withType(KotlinNativeTarget::class.java) {
        binaries.all {
            binaryOptions["memoryModel"] = "experimental"
        }
    }
}

tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack") {
    outputFileName = "js.js"
}
