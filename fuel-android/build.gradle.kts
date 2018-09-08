import com.android.build.gradle.BaseExtension
import com.dicedmelon.gradle.jacoco.android.JacocoAndroidUnitTestReportExtension

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

apply(plugin = "jacoco-android")

dependencies {
    api(project(":fuel"))
    implementation(Dependencies.kotlinStdlib)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.junit)
}

configure<BaseExtension> {
    compileSdkVersion(Versions.fuelCompileSdkVersion)

    defaultConfig {
        minSdkVersion(Versions.fuelMinSdkVersion)
        targetSdkVersion(Versions.fuelCompileSdkVersion)
        versionCode = 1
        versionName = Versions.publishVersion
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }

    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_6)
        setTargetCompatibility(JavaVersion.VERSION_1_6)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    lintOptions {
        isAbortOnError = false
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

configure<JacocoAndroidUnitTestReportExtension> {
    csv.enabled(false)
    html.enabled(true)
    xml.enabled(true)
}
