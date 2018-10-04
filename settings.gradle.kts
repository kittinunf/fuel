pluginManagement {
    repositories {
        maven { setUrl("http://dl.bintray.com/kotlin/kotlin-eap") }
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
}
include(":fuel")
include(":fuel-coroutines")
include(":fuel-forge")
include(":fuel-gson")
include(":fuel-livedata")
include(":fuel-rxjava")
include(":fuel-android")
include(":fuel-jackson")
include(":fuel-moshi")
include(":sample")
include(":sample-java")
