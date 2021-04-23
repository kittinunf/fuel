import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    `maven-publish`
}

group = "fuel"

kotlin {
    /* Targets configuration omitted. 
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */
    jvm()
    js().browser()

    // workaround 1: select iOS target platform depending on the Xcode environment variables
    val iOSTarget: (String) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64
    iOSTarget("ios")

    cocoapods {
        ios.deploymentTarget = "9.0"

        // Configure fields required by CocoaPods.
        authors = "Kittinun Vantasin"
        license = "MIT"
        summary = "Some description for a Kotlin/Native module"
        homepage = "https://github.com/kittinunf/fuel"

        pod("AFNetworking") {
            version = "~> 4.0"
        }
        frameworkName = "FuelCore"
    }

    explicitApi()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                api("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
            }
        }
        val jvmTest by getting
        val jsMain by getting
        val iosMain by getting
    }
}
