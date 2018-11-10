import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    id(Android.appPlugin)
    id(Kotlin.androidPlugin)
    id(Kotlin.androidExtensionsPlugin)
}

dependencies {
    implementation(Kotlin.stdlib)
    implementation(Android.Support.appCompat)
    implementation(KotlinX.Coroutines.android)
    implementation(RxJava.Android.dependency)
    implementation(Stetho.plugin)
    implementation(Stetho.StethoUrlConnection.plugin)

    api(project(":fuel-rxjava"))
    api(project(":fuel-android"))
    api(project(":fuel-livedata"))
    api(project(":fuel-gson"))
    api(project(":fuel-coroutines"))
    api(project(":fuel-stetho"))

    androidTestImplementation(Android.Support.annotation)
    androidTestImplementation(Android.Test.runner)
    androidTestImplementation(Android.Test.rules)
    androidTestImplementation(Android.Espresso.core)
    androidTestImplementation(Android.Espresso.intents)
}

configure<BaseExtension> {
    compileSdkVersion(Fuel.compileSdkVersion)

    defaultConfig {
        applicationId = "com.example.fuel"
        minSdkVersion(Fuel.minSdkVersion)
        targetSdkVersion(Fuel.compileSdkVersion)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    packagingOptions {
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE.txt")
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "android.arch.lifecycle") {
            useVersion("1.1.1")
        }
    }
}
