plugins { java }

dependencies {
    compile(project(":fuel"))
    compile("io.reactivex.rxjava2:rxjava:${extra["rxjavaVersion"]}")
}
