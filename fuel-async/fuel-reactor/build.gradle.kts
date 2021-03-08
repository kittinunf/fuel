apply(plugin = "com.vanniktech.maven.publish")

val okhttpVersion: String by extra

dependencies {
    api(project(":fuel-base"))
    api("io.projectreactor:reactor-core:3.4.3")

    testImplementation("io.projectreactor:reactor-test:3.4.3")
    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}