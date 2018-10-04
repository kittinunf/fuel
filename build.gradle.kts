import com.android.build.gradle.BaseExtension
import com.dicedmelon.gradle.jacoco.android.JacocoAndroidUnitTestReportExtension
import com.jfrog.bintray.gradle.BintrayExtension
import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        google()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
    }

    dependencies {
        classpath(Plugins.android)
        classpath(Plugins.jacocoAndroid)
        classpath(Plugins.bintray)
        classpath(Plugins.kotlin)
    }
}

plugins { java }

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
            plugin("com.android.library")
            plugin("kotlin-android")
            plugin("kotlin-android-extensions")
            plugin("jacoco-android")
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
            plugin("com.novoda.bintray-release")
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
