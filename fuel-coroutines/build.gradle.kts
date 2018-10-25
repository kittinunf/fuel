import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(KotlinX.Coroutines.jvm)
    testCompile(MockServer.dependency)
}