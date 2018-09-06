import com.dicedmelon.gradle.jacoco.android.JacocoAndroidUnitTestReportExtension
import com.jfrog.bintray.gradle.BintrayExtension
import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    apply(from = "versions.gradle.kts")

    repositories {
        mavenCentral()
        jcenter()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.1.3")
        classpath("com.dicedmelon.gradle:jacoco-android:0.1.3")
        classpath("com.novoda:bintray-release:0.8.0")
        classpath(kotlin("gradle-plugin", version = "${extra["kotlinVersion"]}"))
    }
}

plugins { java }

allprojects {
    apply(from = rootProject.projectDir.resolve("versions.gradle.kts"))

    repositories {
        mavenCentral()
        jcenter()
        google()
    }
}

subprojects {
    val isAndroidModule = project.name == "fuel-android" || project.name == "fuel-livedata"
    val isSample = project.name.contains("sample")
    val isJvmModule = !isAndroidModule && !isSample

    if (isJvmModule) {
        apply(plugin = "java")
        apply(plugin = "kotlin")
        apply(plugin = "jacoco")

        dependencies {
            compile("org.jetbrains.kotlin:kotlin-stdlib:${extra["kotlinVersion"]}")
            testCompile("junit:junit:${extra["junitVersion"]}")
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

    if (!isSample) {
        apply(plugin = "com.novoda.bintray-release")

        configure<PublishExtension> {
            artifactId = project.name
            autoPublish = true
            desc = "The easiest HTTP networking library in Kotlin/Android"
            groupId = "com.github.kittinunf.fuel"
            setLicences("MIT")
            publishVersion = extra["publishVersion"].toString()
            uploadName = "Fuel-Android"
            website = "https://github.com/kittinunf/Fuel"
        }
    }
}