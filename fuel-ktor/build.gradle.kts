apply(plugin = "com.vanniktech.maven.publish")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
}

dependencies {
    implementation(Library.KTOR_CLIENT_JVM)
    implementation(project(":fuel-base"))

    testImplementation(Library.JUNIT)
}
