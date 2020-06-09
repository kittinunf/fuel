val ktlint: Configuration by configurations.creating

dependencies {
    ktlint ("com.pinterest:ktlint:0.37.1")
}

tasks.register<JavaExec>("ktlint") {
    group = "verification"
    description = "Check Kotlin code style."
    main = "com.pinterest.ktlint.Main"
    classpath = configurations.getByName("ktlint")
    args = listOf("src/**/*.kt")
}

tasks.getByName("check").dependsOn(tasks.getByName("ktlint"))

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    main = "com.pinterest.ktlint.Main"
    classpath = configurations.getByName("ktlint")
    args = listOf("-F", "src/**/*.kt")
}
