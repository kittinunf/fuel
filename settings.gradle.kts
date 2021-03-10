pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
rootProject.name = "Fuel"

include(":fuel-base")
include(":fuel-singleton")
include(":fuel-moshi")
include(":fuel-kotlinx-serialization")
include(":fuel-forge")
include(":fuel-jackson")
include(":fuel-ktor")

// include(":fuel-samples:simple-client")
include(":fuel-samples:progress")
// include(":fuel-samples:weather")
