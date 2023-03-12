plugins {
    kotlin("js")
}

kotlin {
    js(LEGACY) {
        browser()
        binaries.executable()
    }
}

dependencies {
    implementation(project(":fuel"))
}
