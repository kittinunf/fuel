apply(plugin = "com.vanniktech.maven.publish")

val okhttpVersion: String by extra

dependencies {
    api(project(":fuel-base"))
    api( "com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")

    testImplementation(project(":fuel-singleton"))
    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}
