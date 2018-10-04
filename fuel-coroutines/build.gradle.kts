import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins { java
    kotlin("jvm")
}

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.kotlinCoroutinesJvm)
    testCompile(Dependencies.mockServer)
}