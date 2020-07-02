/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
// Copied From https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-okhttp/jvm/test/io/ktor/client/engine/okhttp/OkHttpEngineTests.kt

package fuel.ktor

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class OkHttpEngineTests {
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    @InternalAPI
    @Test
    fun closeTest() {
        val okHttpClient = OkHttpClient()
        val engine = FuelEngine(FuelConfig().apply { preconfigured = okHttpClient })
        engine.close()

        assertFalse("OkHttp dispatcher is not working.", okHttpClient.dispatcher.executorService.isShutdown)
        assertEquals(0, okHttpClient.connectionPool.connectionCount())
        okHttpClient.cache?.let {
            assertFalse("OkHttp client cache is closed.", it.isClosed)
        }
    }

    @Test
    fun preconfiguresTest() = runBlocking {
        var preconfiguredClientCalled = false
        val okHttpClient = OkHttpClient().newBuilder().addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                preconfiguredClientCalled = true
                return chain.proceed(chain.request())
            }
        }).connectTimeout(1, TimeUnit.MILLISECONDS).build()

        HttpClient(Fuel) {
            engine { preconfigured = okHttpClient }
        }.use { client ->
            runCatching { client.get<String>("http://www.httpbin.org") }
            assertTrue(preconfiguredClientCalled)
        }
    }
}