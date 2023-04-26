# Fuel

[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.20-blue.svg)](http://kotlinlang.org)
[![MavenCentral](https://maven-badges.herokuapp.com/maven-central/com.github.kittinunf.fuel/fuel-jvm/badge.svg)](https://search.maven.org/search?q=com.github.kittinunf.fuel)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io)
![Run Gradle](https://github.com/kittinunf/fuel/workflows/Run%20Gradle/badge.svg?branch=main)
[![Codecov](https://codecov.io/github/kittinunf/fuel/coverage.svg?branch=main)](https://codecov.io/gh/kittinunf/fuel/branch/main)

The easiest HTTP networking library for Kotlin backed by Kotlinx Coroutines.

## Migration
From 3.x onwards, we are using [main](https://github.com/kittinunf/fuel/tree/main) as our new base branch. If you are finding the old version [2.x](https://github.com/kittinunf/fuel/tree/2.x), please take a look at our old branch.

## Download

### For release version
```kotlin
implementation("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")
```

## Quick Start

use the `any http method` [suspend](https://kotlinlang.org/docs/reference/coroutines/basics.html) function:

```kotlin
runBlocking {
    val string = Fuel.get("https://publicobject.com/helloworld.txt").body!!.string()
    println(string)
}

runBlocking {
    val string = "https://publicobject.com/helloworld.txt".httpGet().body!!.string()
    println(string)
}

```

Please note it will throw Exceptions. Make sure you catch it on the production apps.

Fuel requires Java 8 byte code.

## Requirements
- If you are using Android, It needs to be Android 5+.
- Java 8+

## R8 / Proguard

Fuel is fully compatible with R8 out of the box and doesn't require adding any extra rules.

If you use Proguard, you may need to add rules for [Coroutines](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/resources/META-INF/proguard/coroutines.pro), [OkHttp](https://github.com/square/okhttp/blob/master/okhttp/src/main/resources/META-INF/proguard/okhttp3.pro) and [Okio](https://github.com/square/okio/blob/master/okio/src/jvmMain/resources/META-INF/proguard/okio.pro).

If you use the fuel-serialization modules, you may need to add rules for [Serialization](https://github.com/Kotlin/kotlinx.serialization#androidjvm).

If you use the fuel-moshi modules, you may need to add rules for [Moshi](https://github.com/square/moshi/blob/master/moshi/src/main/resources/META-INF/proguard/moshi.pro) and [Moshi-Kotlin](https://github.com/square/moshi/blob/master/kotlin/reflect/src/main/resources/META-INF/proguard/moshi-kotlin.pro)

## Other libraries

If you like Fuel, you might also like other libraries of mine;
* [Result](https://github.com/kittinunf/Result) - The modelling for success/failure of operations in Kotlin
* [Fuse](https://github.com/kittinunf/Fuse) - A simple generic LRU memory/disk cache for Android written in Kotlin
* [Forge](https://github.com/kittinunf/Forge) - Functional style JSON parsing written in Kotlin
* [ReactiveAndroid](https://github.com/kittinunf/ReactiveAndroid) - Reactive events and properties with RxJava for Android SDK

## Credits

Fuel brought to you by [contributors](https://github.com/kittinunf/Fuel/graphs/contributors).

## Licenses

Fuel released under the [MIT](https://opensource.org/licenses/MIT) license.
