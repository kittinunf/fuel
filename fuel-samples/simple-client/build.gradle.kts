plugins {
    kotlin("kapt")
}

application {
    mainClass.set("fuel.samples.FuelContributorsKt")
}

val moshiVersion: String by extra

dependencies {
    kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    implementation(project(":fuel-singleton"))
    implementation(project(":fuel-moshi"))
}
