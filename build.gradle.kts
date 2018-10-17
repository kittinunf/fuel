import com.android.build.gradle.BaseExtension
import com.dicedmelon.gradle.jacoco.android.JacocoAndroidUnitTestReportExtension
import com.jfrog.bintray.gradle.BintrayExtension
import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
//    kotlin("jvm") version Versions.kotlinVersion apply false
    id(Plugins.kotlinJvm) version Versions.kotlinVersion apply false
    id(Plugins.androidLib) version "3.1.3" apply false
    id(Plugins.jacocoAndroid) version "0.1.3" apply false
    id(Plugins.bintrayRelease) version "0.8.0" apply false
    id(Plugins.serialization) version Versions.kotlinVersion apply false
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
    }
}

val androidModules = listOf("fuel-android", "fuel-livedata")
val androidSampleModules = listOf("sample", "sample-java")

subprojects {
    val isAndroidModule = project.name in androidModules
    val isSample = project.name in androidSampleModules
    val isJvmModule = !isAndroidModule && !isSample

    if (isJvmModule) {
        apply {
            plugin("java")
            plugin("kotlin")
            plugin("jacoco")
        }

        configure<JacocoPluginExtension> {
            toolVersion = Versions.jacocoVersion
        }

        dependencies {
            compile(Dependencies.kotlinStdlib)
            testCompile(Dependencies.junit)
        }

        configure<JavaPluginConvention> {
            sourceCompatibility = JavaVersion.VERSION_1_6
            targetCompatibility = JavaVersion.VERSION_1_6

            sourceSets {
                getByName("main").java.srcDirs("src/main/kotlin")
                getByName("test").java.srcDirs("src/main/kotlin")
            }
        }

        tasks.withType<JacocoReport> {
            reports {
                html.isEnabled = false
                xml.isEnabled = true
                csv.isEnabled = false
            }
        }
    }

    if (isAndroidModule) {
        apply {
            plugin(Plugins.androidLib)
            plugin(Plugins.kotlinAndroid)
            plugin(Plugins.kotlinAndroidExtensions)
            plugin(Plugins.jacocoAndroid)
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
    }

    if (!isSample) {
        apply {
            plugin(Plugins.bintrayRelease)
        }

        configure<PublishExtension> {
            artifactId = project.name
            autoPublish = true
            desc = "The easiest HTTP networking library in Kotlin/Android"
            groupId = "com.github.kittinunf.fuel"
            setLicences("MIT")
            publishVersion = Versions.publishVersion
            uploadName = "Fuel-Android"
            website = "https://github.com/kittinunf/Fuel"
        }
    }
}
