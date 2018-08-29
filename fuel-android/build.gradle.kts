import com.android.build.gradle.BaseExtension
import com.dicedmelon.gradle.jacoco.android.JacocoAndroidUnitTestReportExtension
import com.novoda.gradle.release.PublishExtension

plugins { java }

apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.novoda.bintray-release")
apply(plugin = "jacoco-android")

configure<BaseExtension> {
    compileSdkVersion = extra["fuelCompileSdkVersion"].toString()

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(extra["fuelCompileSdkVersion"].toString())
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

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib:${extra["kotlinVersion"]}")

    compile(project(":fuel"))

    testCompile("junit:junit:${extra["junitVersion"]}")
    testCompile("org.robolectric:robolectric:${extra["robolectricVersion"]}")
}

configure<PublishExtension> {
    artifactId = "fuel-android"
    autoPublish = true
    desc = "The easiest HTTP networking library in Kotlin/Android"
    groupId = "com.github.kittinunf.fuel"
    setLicences("MIT")
    publishVersion = extra["publishVersion"].toString()
    uploadName = "Fuel-Android"
    website = "https://github.com/kittinunf/Fuel"
}

tasks.withType<Javadoc> {
    enabled = false
}
