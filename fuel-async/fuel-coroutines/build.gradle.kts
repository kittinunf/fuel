apply(plugin = "com.vanniktech.maven.publish")

val okhttpVersion: String by extra

dependencies {
    api(project(":fuel-base"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}