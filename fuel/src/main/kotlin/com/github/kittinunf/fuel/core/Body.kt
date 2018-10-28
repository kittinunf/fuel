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
    fun writeTo(outputStream: OutputStream, charset: Charset? = null): Long
    fun isEmpty(): Boolean
    fun isConsumed(): Boolean

    val length: Number?
}

data class DefaultBody(
    private var openStream: BodySource? = null,
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

    override fun writeTo(outputStream: OutputStream, charset: Charset?): Long {
        // This actually writes whatever the body is outputting with the given charset.
        return outputStream.buffered().let {
            val stream = openStream!!.invoke().buffered()
            val length = stream.copyTo(it)
            stream.close()

            // The outputStream is buffered, but we are done reading, so it's time to flush what's left
            it.flush()

            // This prevents implementations from writing the body twice
            openStream = CONSUMED_READER

            length
        }
    }

    override fun isEmpty() = openStream == null || (length != null && length == 0)
    override fun isConsumed() = openStream === CONSUMED_READER

    override val length: Number? by lazy {
        calculateLength?.invoke()
    }

    companion object {

        val CONSUMED_READER = {
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