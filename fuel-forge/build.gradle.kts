plugins { java }

dependencies {
    compile(project(":fuel"))
    compile(Forge.dependency)
    testCompile(project(":fuel-test"))
}
