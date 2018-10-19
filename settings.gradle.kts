pluginManagement {
    repositories {
        maven(url = "http://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        jcenter()
        google()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == Kotlin.jvmId) {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
            if(requested.id.id == Android.libPlugin) {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
            if(requested.id.id == Jacoco.Android.plugin) {
                useModule("com.dicedmelon.gradle:jacoco-android:${requested.version}")
            }
            if(requested.id.id == BintrayRelease.plugin) {
                useModule("com.novoda:bintray-release:${requested.version}")
            }
            if(requested.id.id == KotlinX.Serialization.plugin) {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}
include(":fuel")
include(":fuel-coroutines")
include(":fuel-forge")
include(":fuel-gson")
include(":fuel-livedata")
include(":fuel-reactor")
include(":fuel-rxjava")
include(":fuel-android")
include(":fuel-jackson")
include(":fuel-kotlinx-serialization")
include(":fuel-moshi")
include(":sample")
include(":sample-java")
