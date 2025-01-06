import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
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
    iosX64 {
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

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.io.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        jvmMain {
            dependencies {
                api(libs.okhttp.coroutines)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.mockwebserver)
            }
        }
    }
}

dependencies {
    kover(project(":fuel-forge-jvm"))
    kover(project(":fuel-jackson-jvm"))
    kover(project(":fuel-kotlinx-serialization"))
    kover(project(":fuel-moshi-jvm"))
}
