import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION

apply(plugin = "com.vanniktech.maven.publish")

dependencies {
    api(kotlin("stdlib-jdk8", VERSION))
    api(Library.KOTLINX_COROUTINES_CORE)
    api(Library.OKHTTP)

    testImplementation(Library.JUNIT)
    testImplementation(Library.OKHTTP_MOCK_WEB_SERVER)
}
