plugins {
    application
    kotlin("jvm")
}

version = "2.3.0"
group = "com.github.kittinunf"

application {
    mainClassName = "com.github.kittinunf.fuel.MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.kittinunf.fuel:fuel:2.3.0")

    testImplementation("junit:junit:4.12")
}
