import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins { java }

dependencies {
    compile(Dependencies.result)
    testCompile(Dependencies.json)
}
