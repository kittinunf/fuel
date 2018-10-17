import org.gradle.plugin.use.PluginDependenciesSpec

object PluginID {
    const val kotlinJvm = "org.jetbrains.kotlin.jvm"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val androidLib = "com.android.library"
    const val jacoco = "jacoco"
    const val jacocoAndroid = "jacoco-android"
    const val bintrayRelease = "com.novoda.bintray-release"
    const val serialization = "kotlinx-serialization"
}

//val PluginDependenciesSpec.androidLib
//    get() = id(PluginID.androidLib)
//        .version("3.1.3")
//        .apply(false)