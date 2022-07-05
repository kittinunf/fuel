plugins {
    kotlin("jvm")
    kotlin("kapt")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

/*tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}*/

dependencies {
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    implementation(project(":fuel"))
    implementation(project(":fuel-moshi-jvm"))
}
