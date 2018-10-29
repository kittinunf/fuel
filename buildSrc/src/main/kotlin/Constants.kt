// Library version
object Fuel {
    const val publishVersion = "1.16.0"

    const val compileSdkVersion = 27
    const val minSdkVersion = 19
}

// Core dependencies
object Kotlin {
    const val version = "1.3.0"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Kotlin.version}"
    const val plugin = "kotlin"
    const val androidPlugin = "kotlin-android"
    const val androidExtensionsPlugin = "kotlin-android-extensions"
}

object Result {
    const val version = "1.6.0"
    const val dependency = "com.github.kittinunf.result:result:$version"
}

object Json {
    const val version = "20170516"
    const val dependency = "org.json:json:$version"
}

object Android {
    const val version = "3.1.3"
    const val appPlugin = "com.android.application"
    const val libPlugin = "com.android.library"

    object Support {
        const val version = "27.1.1"
        val annotation = "com.android.support:support-annotations:$version"
        val appCompat = "com.android.support:appcompat-v7:$version"
    }

    object Arch {
        const val version = "1.1.1"
        const val extensions = "android.arch.lifecycle:extensions:$version"
    }

    object Espresso {
        const val version = "3.0.0"
        const val core = "com.android.support.test.espresso:espresso-core:$version"
        const val intents = "com.android.support.test.espresso:espresso-intents:$version"
    }

    // Testing dependencies
    object Test {
        const val rulesVersion = "1.0.0"
        const val runnerVersion = "1.0.0"
        val rules = "com.android.support.test:rules:$rulesVersion"
        val runner = "com.android.support.test:runner:$runnerVersion"
    }
}

// Modules dependencies
object Forge {
    const val version = "0.3.0"
    const val dependency = "com.github.kittinunf.forge:forge:$version"
}

object Gson {
    const val version = "2.8.2"
    const val dependency = "com.google.code.gson:gson:$version"
}

object Jackson {
    const val version = "2.9.6"
    const val dependency = "com.fasterxml.jackson.module:jackson-module-kotlin:$version"
}

object KotlinX {
    object Coroutines {
        const val version = "1.0.0"
        val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        val jvm = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    }

    object Serialization {
        const val version = "0.9.0"
        const val dependency = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:$version"
        const val plugin = "kotlinx-serialization"
    }
}

object Moshi {
    const val version = "1.6.0"
    const val dependency = "com.squareup.moshi:moshi:$version"
}

object Reactor {
    const val version = "3.2.0.M4"
    const val core = "io.projectreactor:reactor-core:$version"
    const val test = "io.projectreactor:reactor-test:$version"
}

object RxJava {
    object Jvm {
        const val version = "2.1.13"
        const val dependency = "io.reactivex.rxjava2:rxjava:$version"
    }
    object Android {
        const val version = "2.1.0"
        val dependency = "io.reactivex.rxjava2:rxandroid:$version"
    }
}

// Lint
object Ktlint {
    const val version = "1.20.1"
    const val plugin = "org.jmailen.kotlinter"
}

// Testing dependencies
object JUnit {
    const val version = "4.12"
    const val dependency = "junit:junit:$version"
}

object MockServer {
    const val version = "5.4.1"
    const val dependency = "org.mock-server:mockserver-netty:$version"
}

object Jacoco {
    const val version = "0.8.2"
    const val plugin = "jacoco"

    object Android {
        const val version = "0.1.3"
        const val plugin = "jacoco-android"
    }
}

object RoboElectric {
    const val version = "3.8"
    const val dependency = "org.robolectric:robolectric:$version"
}

object BintrayRelease {
    const val version = "0.8.0"
    const val plugin = "com.novoda.bintray-release"
}
