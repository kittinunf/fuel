pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == Android.libPlugin) {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
            if (requested.id.id == Jacoco.Android.plugin) {
                useModule("com.dicedmelon.gradle:jacoco-android:${requested.version}")
            }
            if (requested.id.id == KotlinX.Serialization.plugin) {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}
val projects = listOf(
        Fuel.name,
        Fuel.Android.name,
        Fuel.Coroutines.name,
        Fuel.Forge.name,
        Fuel.Gson.name,
        Fuel.Jackson.name,
        Fuel.Json.name,
        Fuel.KotlinSerialization.name,
        Fuel.LiveData.name,
        Fuel.Moshi.name,
        Fuel.Reactor.name,
        Fuel.RxJava.name,
        Fuel.Stetho.name,
        Fuel.Test.name
)

include(*(projects.toTypedArray()))

val includeSample: String by settings
if (includeSample == "true") {
    include(":sample")
}
