plugins {
    kotlin("multiplatform")
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
    val iOSTarget: (String) -> org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64
    iOSTarget("ios")

    explicitApi()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":fuel-core"))
            }
        }
        val jvmMain by getting
        val jsMain by getting
        val iosMain by getting
    }
}
