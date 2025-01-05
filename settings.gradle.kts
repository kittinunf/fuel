rootProject.name = "Fuel-MPP"

include(":fuel")
include(":fuel-forge-jvm")
include(":fuel-jackson-jvm")
include(":fuel-kotlinx-serialization")
include(":fuel-moshi-jvm")

// include(":samples:httpbin-wasm")
include(":samples:mockbin-native")

pluginManagement {
    includeBuild("plugins")
}
