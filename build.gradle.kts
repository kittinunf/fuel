import com.android.build.gradle.BaseExtension
import com.dicedmelon.gradle.jacoco.android.JacocoAndroidUnitTestReportExtension
import com.jfrog.bintray.gradle.BintrayExtension
import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.1.3")
        classpath("com.dicedmelon.gradle:jacoco-android:0.1.3")
        classpath("com.novoda:bintray-release:0.8.0")
        classpath(kotlin("gradle-plugin", version = Versions.kotlinVersion))
    }
}

plugins { java }

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
    }
}

val androidModules = listOf("fuel-android", "fuel-livedata")
val androidSampleModules = listOf("sample", "sample-java")

subprojects {
    val isAndroidModule = project.name in androidModules
    val isSample = project.name in androidSampleModules
    val isJvmModule = !isAndroidModule && !isSample

    if (isJvmModule) {
        apply(plugin = "java")
        apply(plugin = "kotlin")
        apply(plugin = "jacoco")

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

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xdisable-default-scripting-plugin")
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
        apply(plugin = "com.android.library")
        apply(plugin = "kotlin-android")
        apply(plugin = "kotlin-android-extensions")
        apply(plugin = "jacoco-android")

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
        apply(plugin = "com.novoda.bintray-release")

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
