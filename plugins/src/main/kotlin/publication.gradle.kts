import java.util.Properties

plugins {
    java
    `maven-publish`
    signing
}

ext["signing.key"] = null
ext["signing.password"] = null
ext["sonatype.username"] = null
ext["sonatype.password"] = null

val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply { load(it) }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.key"] = System.getenv("SIGNING_KEY")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["sonatype.username"] = System.getenv("SONATYPE_USERNAME")
    ext["sonatype.password"] = System.getenv("SONATYPE_PASSWORD")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")

publishing {
    repositories {
        maven {
            name = "sonatype"
            url = uri(
                if (isReleaseBuild) {
                    "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                } else {
                    "https://oss.sonatype.org/content/repositories/snapshots"
                }
            )

            credentials {
                username = getExtraString("sonatype.username")
                password = getExtraString("sonatype.password")
            }
        }
    }

    val sources by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(java.sourceSets.main.get().allSource)
    }

    // Creating maven artifacts for jvm
    publications {
        if (project.name.substringAfterLast("-") == "jvm") {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        val artifactName: String by project
        val artifactDesc: String by project
        val artifactUrl: String by project
        val artifactScm: String by project
        val artifactLicenseName: String by project
        val artifactLicenseUrl: String by project

        artifactId = project.name

        artifact(javadocJar)
        if (project.name.substringAfterLast("-") == "jvm") {
            artifact(sources)
        }

        pom {
            name.set(artifactName)
            description.set(artifactDesc)
            url.set(artifactUrl)
            licenses {
                license {
                    name.set(artifactLicenseName)
                    url.set(artifactLicenseUrl)
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("iNoles")
                }
                developer {
                    id.set("kittinunf")
                }
            }
            contributors {
            }
            scm {
                connection.set(artifactScm)
                developerConnection.set(artifactScm)
                url.set(artifactUrl)
            }
        }
    }
}

signing {
    val signingKey = project.ext["signing.key"] as? String
    val signingPassword = project.ext["signing.password"] as? String
    if (signingKey == null || signingPassword == null) return@signing

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}

// TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
    dependsOn(project.tasks.withType(Sign::class.java))
}
