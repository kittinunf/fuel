import com.android.build.gradle.BaseExtension
import com.dicedmelon.gradle.jacoco.android.JacocoAndroidUnitTestReportExtension
import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.support.ReporterType

plugins {
    java
    kotlin("jvm") version Kotlin.version apply false
    id(Android.libPlugin) version Android.version apply false
    id(Jacoco.Android.plugin) version Jacoco.Android.version apply false
    id(BintrayRelease.plugin) version BintrayRelease.version apply false
    id(KotlinX.Serialization.plugin) version Kotlin.version apply false
    id(Ktlint.plugin) version Ktlint.version apply false
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
            plugin(Kotlin.plugin)
            plugin(Jacoco.plugin)
        }

        configure<JacocoPluginExtension> {
            toolVersion = Jacoco.version
        }

        dependencies {
            compile(Kotlin.stdlib)
            testCompile(JUnit.dependency)
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
            plugin(Android.libPlugin)
            plugin(Kotlin.androidPlugin)
            plugin(Kotlin.androidExtensionsPlugin)
            plugin(Jacoco.Android.plugin)
        }

        configure<BaseExtension> {
            compileSdkVersion(Fuel.compileSdkVersion)

            defaultConfig {
                minSdkVersion(Fuel.minSdkVersion)
                targetSdkVersion(Fuel.compileSdkVersion)
                versionCode = 1
                versionName = Fuel.publishVersion
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

        tasks.withType<Javadoc>().all { enabled = false }
    }

    if (!isSample) {
        apply {
            plugin(BintrayRelease.plugin)
            plugin(Ktlint.plugin)
        }

        configure<PublishExtension> {
            artifactId = project.name
            autoPublish = true
            desc = "The easiest HTTP networking library in Kotlin/Android"
            groupId = "com.github.kittinunf.fuel"
            setLicences("MIT")
            publishVersion = Fuel.publishVersion
            uploadName = "Fuel-Android"
            website = "https://github.com/kittinunf/Fuel"
        }

        configure<KotlinterExtension> {
            reporters = arrayOf(ReporterType.plain.name, ReporterType.checkstyle.name)
        }
    }
}
