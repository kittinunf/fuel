plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11_7
    targetCompatibility = JavaVersion.VERSION_11_7
}
