import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.21"
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
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
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
