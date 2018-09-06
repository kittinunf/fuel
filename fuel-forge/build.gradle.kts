plugins { java }

dependencies {
    compile(project(":fuel"))
    compile("com.github.kittinunf.forge:forge:${extra["forgeVersion"]}")
}
