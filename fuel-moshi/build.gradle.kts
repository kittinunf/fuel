plugins { java }
dependencies {
    compile(project(":fuel"))
    compile("com.squareup.moshi:moshi-kotlin:${extra["moshiVersion"]}")
}
