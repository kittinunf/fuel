plugins {
    kotlin("multiplatform")
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
    js {
        browser()
        binaries.executable()
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

    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
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
        appleMain {
            dependencies {
                implementation(libs.okio)
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
