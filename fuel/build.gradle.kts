import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins { java }

dependencies {
    compile("com.github.kittinunf.result:result:${extra["resultVersion"]}")
    testCompile("org.json:json:20170516")
}
