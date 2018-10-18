plugins {
    java
    id(KotlinX.Serialization.plugin)
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    compile(project(":fuel"))
    compile(KotlinX.Serialization.dependency)
    testCompile(MockServer.dependency)
}
