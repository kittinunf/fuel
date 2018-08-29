import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins { java }

apply(plugin = "kotlin")
apply(plugin = "com.novoda.bintray-release")
apply(plugin = "jacoco")

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib:${extra["kotlinVersion"]}")
    compile("com.github.kittinunf.result:result:${extra["resultVersion"]}")

    testCompile("org.json:json:20170516")
    testCompile("junit:junit:${ext["junitVersion"]}")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/main/kotlin")
    }
}

configure<KotlinJvmProjectExtension> {
    experimental.coroutines = Coroutines.ENABLE
}

configure<PublishExtension> {
    artifactId = "fuel"
    autoPublish = true
    desc = "The easiest HTTP networking library in Kotlin/Android"
    groupId = "com.github.kittinunf.fuel"
    setLicences("MIT")
    publishVersion = publishVersion
    uploadName = "Fuel-Android"
    website = "https://github.com/kittinunf/Fuel"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
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
