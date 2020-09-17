/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
// Copied From https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-okhttp/jvm/src/io/ktor/client/engine/okhttp/StreamRequestBody.kt

package fuel.ktor

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

internal class StreamRequestBody(
    private val contentLength: Long?,
    private val block: () -> ByteReadChannel
) : RequestBody() {

    override fun contentType(): MediaType? = null

    override fun writeTo(sink: BufferedSink) {
        block().toInputStream().source().use {
            sink.writeAll(it)
        }
    }

    override fun contentLength(): Long = contentLength ?: -1
}
