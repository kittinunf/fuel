import com.android.build.gradle.BaseExtension
import com.dicedmelon.gradle.jacoco.android.JacocoAndroidUnitTestReportExtension
import com.jfrog.bintray.gradle.BintrayExtension
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.support.ReporterType

plugins {
    java
    kotlin("jvm") version Kotlin.version apply false
    id(Android.libPlugin) version Android.version apply false
    id(Jacoco.Android.plugin) version Jacoco.Android.version apply false
    id(KotlinX.Serialization.plugin) version Kotlin.version apply false
    id(Ktlint.plugin) version Ktlint.version apply false

    maven
    `maven-publish`
    id(Release.Bintray.plugin) version Release.Bintray.version apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
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
            sourceCompatibility = JavaVersion.VERSION_1_7
            targetCompatibility = JavaVersion.VERSION_1_7

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
                setSourceCompatibility(JavaVersion.VERSION_1_7)
                setTargetCompatibility(JavaVersion.VERSION_1_7)
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
            plugin("org.gradle.maven-publish")

            plugin(Release.Bintray.plugin)
            plugin(Ktlint.plugin)
        }

        configure<BintrayExtension> {
            user = findProperty("BINTRAY_USER") as? String
            key = findProperty("BINTRAY_KEY") as? String
            setConfigurations("archives")
            pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
                repo = "maven"
                name = "Fuel-Android"
                desc = "The easiest HTTP networking library in Kotlin/Android"
                userOrg = "kittinunf"
                websiteUrl = "https://github.com/kittinunf/Fuel"
                vcsUrl = "https://github.com/kittinunf/Fuel"
                setLicenses("MIT")
            })
        }

        configure<KotlinterExtension> {
            reporters = arrayOf(ReporterType.plain.name, ReporterType.checkstyle.name)
        }

        if (project.hasProperty("android").not()) {
            val sourcesJar by tasks.registering(Jar::class) {
                classifier = "sources"
                from(sourceSets["main"].allSource)
            }

            publishing {
                publications {
                    register(project.name, MavenPublication::class) {
                        from(components["java"])
                        artifact(sourcesJar.get())
                        groupId = "com.github.kittinunf.fuel"
                        artifactId = project.name
                        version = Fuel.publishVersion
                    }
                }
            }
        }
    }
}
