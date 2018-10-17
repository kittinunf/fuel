pluginManagement {
    repositories {
        maven(url = "http://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
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
