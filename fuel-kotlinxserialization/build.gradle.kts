//buildscript {
//    repositories {
//        maven { setUrl("https://kotlin.bintray.com/kotlinx") }
//    }
//
//    dependencies {
////        classpath("org.jetbrains.kotlinx:kotlinx-gradle-serialization-plugin:0.6.2")
////        classpath(Plugins.serialization)
//    }
//}

plugins { java }

apply {
//    plugin("kotlinx-serialization")
}

repositories {
    maven { setUrl("https://kotlin.bintray.com/kotlinx") }
}

dependencies {
    compile(project(":fuel"))
    compile(Dependencies.kotlinXSerialization)
    testCompile(Dependencies.mockServer)
}
