import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins { java }

dependencies {
    compile(project(":fuel"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:${extra["kotlinCoroutinesVersion"]}")
    testCompile(project(":fuel-jackson"))
}

configure<KotlinJvmProjectExtension> {
    experimental.coroutines = Coroutines.ENABLE
}