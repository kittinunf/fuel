import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${extra["kotlinVersion"]}")

    implementation("com.android.support:appcompat-v7:${extra["androidSupportVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${extra["kotlinCoroutinesVersion"]}")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.2")

    api(project(":fuel-rxjava"))
    api(project(":fuel-android"))
    api(project(":fuel-livedata"))
    api(project(":fuel-gson"))
    api(project(":fuel-coroutines"))

    androidTestImplementation("com.android.support:support-annotations:${extra["androidSupportVersion"]}")
    androidTestImplementation("com.android.support.test:runner:${extra["runnerVersion"]}")
    androidTestImplementation("com.android.support.test:rules:${extra["rulesVersion"]}")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:${extra["espressoVersion"]}")
    androidTestImplementation("com.android.support.test.espresso:espresso-intents:${extra["espressoVersion"]}")
}

configure<BaseExtension> {
    compileSdkVersion(extra["fuelCompileSdkVersion"] as Int)

    defaultConfig {
        applicationId = "com.example.fuel"
        minSdkVersion(16)
        targetSdkVersion(extra["fuelCompileSdkVersion"] as Int)
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

configure<KotlinProjectExtension> {
    experimental.coroutines = Coroutines.ENABLE
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "android.arch.lifecycle") {
            useVersion("1.1.1")
        }
    }
}
