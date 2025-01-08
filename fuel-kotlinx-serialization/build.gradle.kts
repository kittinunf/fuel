import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    id("publication")
    alias(libs.plugins.kover)
}

kotlin {
    jvm {
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
