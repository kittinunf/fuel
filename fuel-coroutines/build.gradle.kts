
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(KotlinX.Coroutines.jvm)
    testCompile(project(":fuel-test"))
}