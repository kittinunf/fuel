import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.kotlinCoroutinesJvm)
    testCompile(Dependencies.mockServer)
}

configure<KotlinJvmProjectExtension> {
    experimental.coroutines = Coroutines.ENABLE
}