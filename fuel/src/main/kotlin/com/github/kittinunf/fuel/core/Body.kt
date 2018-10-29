package com.github.kittinunf.fuel.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

typealias BodySource = (() -> InputStream)
typealias BodyLength = (() -> Number)

interface Body {
    fun toByteArray(): ByteArray
    fun toStream(): InputStream

    fun writeTo(outputStream: OutputStream, charset: Charset? = null)
    fun isEmpty(): Boolean
    fun isConsumed(): Boolean

    val length: Number?
}

data class DefaultBody(
    internal var openStream: BodySource? = null,
    private var calculateLength: BodyLength? = null,
    val charset: Charset = Charsets.UTF_8
) : Body {

    override fun toByteArray(): ByteArray {
        if (isEmpty()) {
            return ByteArray(0)
        }

        // Stream may be completely consumed by writeTo. Therefore there is no guarantee that the stream will be
        // available a second time

        return ByteArrayOutputStream(length?.toInt() ?: 32).let {
            writeTo(it, charset)
            it.close()
            it.toByteArray()
        }.apply {
            openStream = { ByteArrayInputStream(this) }
            calculateLength = { this.size }
        }
    }

    override fun toStream(): InputStream = openStream!!.invoke().buffered().apply {
        // The caller is now responsible for this stream. This make sure that you can't call this twice without handling
        // it. The caller must still call `.close()` on the returned value when done.
        openStream = CONSUMED_STREAM
    }

    override fun writeTo(outputStream: OutputStream, charset: Charset?) {
        // This actually writes whatever the body is outputting with the given charset.
        outputStream.buffered().apply {
            val reader = openStream!!.invoke().buffered()
            reader.copyTo(this)
            reader.close()

            // The outputStream is buffered, but we are done reading, so it's time to flush what's left
            flush()

            // This prevents implementations from writing the body twice
            openStream = CONSUMED_STREAM
        }
    }

    override fun isEmpty() = openStream == null || (length != null && length == 0)
    override fun isConsumed() = openStream === CONSUMED_STREAM

    override val length: Number? by lazy {
        calculateLength?.invoke()?.let {
            if (it.toLong() == -1L) { null } else { it }
        }
    }

    companion object {

        val CONSUMED_STREAM = {
            throw IllegalStateException(
                "The input has already been written to an output stream and can not be consumed again."
            )
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