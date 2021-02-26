apply(plugin = "com.vanniktech.maven.publish")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
}

dependencies {
    implementation("io.ktor:ktor-client-core-jvm:1.5.1")
    implementation(project(":fuel-base"))

    testImplementation("junit:junit:4.13")
}
