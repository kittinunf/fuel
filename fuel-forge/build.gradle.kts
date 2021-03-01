apply(plugin = "com.vanniktech.maven.publish")

val okhttpVersion: String by extra

dependencies {
    api(project(":fuel-base"))
    api("com.github.kittinunf.forge:forge:1.0.0-alpha3")
    api("com.github.kittinunf.result:result:1.3.0")

    testImplementation(project(":fuel-singleton"))
    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}
