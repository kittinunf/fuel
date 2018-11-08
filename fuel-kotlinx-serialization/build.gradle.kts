plugins {
    java
    id(KotlinX.Serialization.plugin)
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    compile(project(":fuel"))
    compile(KotlinX.Serialization.dependency)
    testCompile(project(":fuel-test"))
}
