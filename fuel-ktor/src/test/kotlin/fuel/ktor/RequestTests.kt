/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
// Copied from https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-okhttp/jvm/test/io/ktor/client/engine/okhttp/RequestTests.kt

package fuel.ktor

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.junit.Assert.assertNotNull
import org.junit.Test

class RequestTests {
    class LoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            return chain.proceed(request)
        }
    }

    @Test
    fun testFeatures() = runBlocking {
        val client = HttpClient(Fuel) {
            engine {
                addInterceptor(LoggingInterceptor())
                addNetworkInterceptor(LoggingInterceptor())
            }
        }
        val google = client.get<String>("https://www.google.com")
        assertNotNull(google)
    }
}