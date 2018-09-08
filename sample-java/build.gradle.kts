import com.android.build.gradle.BaseExtension

plugins {
    id("com.android.application")
}

dependencies {
    implementation(Dependencies.androidAppCompat)
    api(project(":fuel-android"))
}

configure<BaseExtension> {
    compileSdkVersion(Versions.fuelCompileSdkVersion)

    defaultConfig {
        applicationId = "com.example.java.fuel"
        minSdkVersion(Versions.fuelMinSdkVersion)
        targetSdkVersion(Versions.fuelCompileSdkVersion)
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
