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
            if (requested.id.id == Plugins.kotlinJvm) {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
            if(requested.id.id == Plugins.androidLib) {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
            if(requested.id.id == Plugins.jacocoAndroid) {
                useModule("com.dicedmelon.gradle:jacoco-android:${requested.version}")
            }
            if(requested.id.id == Plugins.bintrayRelease) {
                useModule("com.novoda:bintray-release:${requested.version}")
            }
            if(requested.id.id == Plugins.serialization) {
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
