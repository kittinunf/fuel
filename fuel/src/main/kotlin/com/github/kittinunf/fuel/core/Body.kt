package com.github.kittinunf.fuel.core

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.Reader
import java.nio.charset.Charset

typealias BodySource = (() -> Reader)
typealias BodyLength = (() -> Number)

interface Body {
    fun toByteArray() : ByteArray
    fun writeTo(outputStream: OutputStream, charset: Charset? = null)
    fun isEmpty() : Boolean
    fun isNotEmpty() : Boolean

    val length: Number?
}

data class DefaultBody(
    private var openReader: BodySource? = null,
    private var calculateLength: BodyLength? = null,
    val charset: Charset = Charsets.UTF_8
) : Body {
    override fun toByteArray(): ByteArray {
        return ByteArrayOutputStream(length?.toInt() ?: 32).let {
            writeTo(it, charset)
            it.close()
            it.toByteArray()
        }
    }

    override fun writeTo(outputStream: OutputStream, charset: Charset?) {
        // This actually writes whatever the body is outputting with the given charset.
        outputStream.bufferedWriter(charset ?: this.charset).apply {
            val reader = openReader!!.invoke().buffered()
            reader.copyTo(this)
            reader.close()

            // The outputStream is buffered, but we are done reading, so it's time to flush what's left
            flush()
        }
    }

    override fun isEmpty(): Boolean {
        return openReader == null  || (length != null && length == 0)
    }

    override fun isNotEmpty(): Boolean = !isEmpty()

    override val length: Number? by lazy {
        calculateLength?.invoke()
    }

    companion object {
        fun from(openReader: BodySource, calculateLength: BodyLength?, charset: Charset = Charsets.UTF_8): Body {
            return DefaultBody(
                openReader = openReader,
                calculateLength = calculateLength,
                charset = charset
            )
        }
    }

}