plugins { java }

apply {
    plugin("kotlinx-serialization")
}

repositories {
    maven { setUrl("https://kotlin.bintray.com/kotlinx") }
}

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.serialization)
    testCompile(Dependencies.mockServer)
}
