buildscript {
    apply(from = "versions.gradle.kts")

    repositories {
        mavenCentral()
        jcenter()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.1.3")
        classpath("com.dicedmelon.gradle:jacoco-android:0.1.3")
        classpath("com.novoda:bintray-release:0.8.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${extra["kotlinVersion"]}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        jcenter()
        google()
    }
}

subprojects {
    buildscript {
        apply(from = rootProject.projectDir.resolve("versions.gradle.kts"))
    }
}
