plugins {
    kotlin("kapt")
}

application {
    mainClass.set("fuel.samples.FuelWeatherKt")
}

val moshiVersion: String by extra

dependencies {
    kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    implementation(project(":fuel-singleton"))
    implementation(project(":fuel-async:fuel-coroutines"))
    implementation(project(":fuel-moshi"))
}
