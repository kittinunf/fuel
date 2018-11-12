package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Body
import com.github.kittinunf.fuel.core.BodyLength
import com.github.kittinunf.fuel.core.BodySource
import com.github.kittinunf.fuel.core.FuelError
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

data class DefaultBody(
    internal var openStream: BodySource = EMPTY_STREAM,
    private var calculateLength: BodyLength? = null,
    val charset: Charset = Charsets.UTF_8
) : Body {

    override fun toByteArray(): ByteArray {
        if (isEmpty()) {
            return ByteArray(0)
        }

        return ByteArrayOutputStream(length?.toInt() ?: 32)
            .use { stream ->
                writeTo(stream)
                stream.toByteArray()
            }
            .also { result ->
                openStream = { ByteArrayInputStream(result) }
                calculateLength = { result.size.toLong() }
            }
    }

    override fun toStream(): InputStream = openStream().buffered().apply {
        // The caller is now responsible for this stream. This make sure that you can't call this twice without handling
        // it. The caller must still call `.close()` on the returned value when done.
        openStream = CONSUMED_STREAM
    }

    override fun writeTo(outputStream: OutputStream): Long {
        val inputStream = openStream()
        // `copyTo` writes efficiently using a buffer. Reading ensured to be buffered by calling `.buffered`
        return inputStream.buffered()
            .use { it.copyTo(outputStream) }
            .also {
                // The outputStream could be buffered, but we are done reading, so it's time to flush what's left
                outputStream.flush()

                // This prevents implementations from consuming the input stream twice
                openStream = CONSUMED_STREAM
            }
    }

    override fun isEmpty() = openStream === EMPTY_STREAM || (length == 0L)
    override fun isConsumed() = openStream === CONSUMED_STREAM

    override val length: Long? by lazy {
        calculateLength?.invoke()?.let {
            if (it == -1L) { null } else { it }
        }
    }

    companion object {
        private val EMPTY_STREAM = {
            ByteArrayInputStream(ByteArray(0))
        }

        private val CONSUMED_STREAM = {
            throw FuelError.wrap(IllegalStateException(
                "The input has already been written to an output stream and can not be consumed again."
            ))
        }

        fun from(openStream: BodySource, calculateLength: BodyLength?, charset: Charset = Charsets.UTF_8): Body {
            return DefaultBody(
                openStream = openStream,
                calculateLength = calculateLength,
                charset = charset
            )
        }
    }
}