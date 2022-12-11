/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
// Inspired By https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-okhttp/jvm/src/io/ktor/client/engine/okhttp/OkUtils.kt

package fuel.ktor

import io.ktor.http.HttpProtocolVersion
import okhttp3.Headers
import okhttp3.Protocol

internal fun Headers.fromOkHttp(): io.ktor.http.Headers = object : io.ktor.http.Headers {
    override val caseInsensitiveName: Boolean = true

    override fun getAll(name: String): List<String>? = this@fromOkHttp.values(name).takeIf { it.isNotEmpty() }

    override fun names(): Set<String> = this@fromOkHttp.names()

    override fun entries(): Set<Map.Entry<String, List<String>>> = this@fromOkHttp.toMultimap().entries

    override fun isEmpty(): Boolean = this@fromOkHttp.size == 0
}

@Suppress("DEPRECATION")
internal fun Protocol.fromOkHttp(): HttpProtocolVersion = when (this) {
    Protocol.HTTP_1_0 -> HttpProtocolVersion.HTTP_1_0
    Protocol.HTTP_1_1 -> HttpProtocolVersion.HTTP_1_1
    Protocol.SPDY_3 -> HttpProtocolVersion.SPDY_3
    Protocol.HTTP_2 -> HttpProtocolVersion.HTTP_2_0
    Protocol.H2_PRIOR_KNOWLEDGE -> HttpProtocolVersion.HTTP_2_0
    Protocol.QUIC -> HttpProtocolVersion.QUIC
    Protocol.HTTP_3 -> HttpProtocolVersion("HTTP", 3, 0)
}
