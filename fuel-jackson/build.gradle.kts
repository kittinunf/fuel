plugins { java }

dependencies {
    compile(project(":fuel"))
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:${extra["jacksonVersion"]}")
}
