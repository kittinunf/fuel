plugins { java }

dependencies {
    compile(project(":fuel"))
    compile("com.google.code.gson:gson:${extra["gsonVersion"]}")
}
