apply(plugin = "com.vanniktech.maven.publish")

val okhttpVersion: String by extra

dependencies {
    api(project(":fuel-base"))
    api("io.reactivex.rxjava3:rxjava:3.0.11")

    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}