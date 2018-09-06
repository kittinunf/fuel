import com.android.build.gradle.BaseExtension

plugins {
    id("com.android.application")
}

dependencies {
    implementation("com.android.support:appcompat-v7:${extra["androidSupportVersion"]}")
    api(project(":fuel-android"))
}

configure<BaseExtension> {
    compileSdkVersion(extra["fuelCompileSdkVersion"] as Int)

    defaultConfig {
        applicationId = "com.example.java.fuel"
        minSdkVersion(16)
        targetSdkVersion(extra["fuelCompileSdkVersion"] as Int)
        versionCode = 1
        versionName = "1.0"
    }

    dataBinding {
        isEnabled = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}
