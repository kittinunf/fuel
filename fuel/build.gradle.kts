import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform")
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
                baseName = "Fuel"
            }
        }
    }
    explicitApi()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.2")
            }
        }
        val jsMain by getting {
            /*dependencies {
                api(npm("node-fetch", "2.6.1"))
                api(npm("abort-controller", "3.0.0"))
            }*/
        }
        val jsTest by getting
        val iosMain by getting
        val iosTest by getting
    }
}

tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack") {
    outputFileName = "js.js"
}
