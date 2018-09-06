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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${extra["kotlinVersion"]}")
    testImplementation("org.robolectric:robolectric:${extra["robolectricVersion"]}")
    testImplementation("junit:junit:${extra["junitVersion"]}")
}

configure<BaseExtension> {
    compileSdkVersion(extra["fuelCompileSdkVersion"] as Int)

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(extra["fuelCompileSdkVersion"] as Int)
        versionCode = 1
        versionName = extra["publishVersion"].toString()
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
