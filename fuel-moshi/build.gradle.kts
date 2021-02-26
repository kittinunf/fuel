apply(plugin = "com.vanniktech.maven.publish")

plugins {
    kotlin("kapt")
}

val okhttpVersion: String by extra
val moshiVersion: String by extra

dependencies {
    api(project(":fuel-base"))
    api("com.squareup.moshi:moshi:$moshiVersion")

    kaptTest("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    testImplementation(project(":fuel-singleton"))
    testImplementation("junit:junit:4.13")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}
