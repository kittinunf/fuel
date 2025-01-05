import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.1.0"
    id("publication")
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    iosArm64 {
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
    iosX64 {
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
        commonMain {
            dependencies {
                api(project(":fuel"))
                api(libs.kotlinx.serialization.json)
                api(libs.result)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.mockwebserver)
            }
        }
    }
}
