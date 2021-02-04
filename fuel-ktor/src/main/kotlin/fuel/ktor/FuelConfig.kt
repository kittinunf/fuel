/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
// Copied By https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-okhttp/jvm/src/io/ktor/client/engine/okhttp/OkHttpConfig.kt

@file:Suppress("MemberVisibilityCanBePrivate")

package fuel.ktor

import io.ktor.client.engine.HttpClientEngineConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient

public class FuelConfig : HttpClientEngineConfig() {
    internal var config: OkHttpClient.Builder.() -> Unit = {
        followRedirects(false)
        followSslRedirects(false)
        retryOnConnectionFailure(false)
    }

    /**
     * Preconfigured [OkHttpClient] instance instead of configuring one.
     */
    public var preconfigured: OkHttpClient? = null

    /**
     * Size of the cache that keeps least recently used [OkHttpClient] instances. Set "0" to avoid caching.
     */
    public var clientCacheSize: Int = 10

    /**
     * Configure [OkHttpClient] using [OkHttpClient.Builder].
     */
    public fun config(block: OkHttpClient.Builder.() -> Unit) {
        val oldConfig = config
        config = {
            oldConfig()
            block()
        }
    }

    /**
     * Add [Interceptor] to [OkHttpClient].
     */
    public fun addInterceptor(interceptor: Interceptor) {
        config {
            addInterceptor(interceptor)
        }
    }

    /**
     * Add network [Interceptor] to [OkHttpClient].
     */
    public fun addNetworkInterceptor(interceptor: Interceptor) {
        config {
            addNetworkInterceptor(interceptor)
        }
    }
}
