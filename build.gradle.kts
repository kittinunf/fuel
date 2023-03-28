plugins {
    kotlin("multiplatform") version "1.8.10" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
    val artifactPublishVersion: String by project
    val artifactGroupId: String by project
    group = artifactGroupId
    version = artifactPublishVersion
}
