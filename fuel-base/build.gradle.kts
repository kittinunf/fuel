import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION

apply(plugin = "com.vanniktech.maven.publish")

val okhttpVersion: String by extra

dependencies {
    api(kotlin("stdlib-jdk8", VERSION))
    api("com.squareup.okhttp3:okhttp:$okhttpVersion")

    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}
