plugins {
    java
    id(Plugins.serialization)
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.serialization)
    testCompile(Dependencies.mockServer)
}
