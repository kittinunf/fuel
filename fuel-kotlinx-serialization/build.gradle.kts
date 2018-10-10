plugins { java }

apply {
    plugin("kotlinx-serialization")
}

repositories {
    maven { setUrl("https://kotlin.bintray.com/kotlinx") }
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
}

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.serialization)
    testCompile(Dependencies.mockServer)
}
