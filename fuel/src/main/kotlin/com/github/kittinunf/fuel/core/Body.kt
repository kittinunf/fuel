package com.github.kittinunf.fuel.core

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset

typealias BodySource = (() -> Reader)

data class Body(
    private var openReader: BodySource? = null,
    val length: Number? = null,
    val charset: Charset = Charsets.UTF_8
) {
    fun toByteArray(): ByteArray {
        return ByteArrayOutputStream(length?.toInt() ?: 32).apply {
            writeTo(this)
        }.toByteArray()
    }

    fun writeTo(outputStream: OutputStream) {
        writeTo(outputStream.writer(charset))
    }

    fun writeTo(outputStream: Writer) {
        outputStream.buffered().apply {
            val reader = openReader!!.invoke().buffered()
            reader.copyTo(this)
            reader.close()
        }
    }

    fun isEmpty(): Boolean {
        return openReader == null  || (length != null && length == 0)
    }

    fun isNotEmpty(): Boolean = !isEmpty()

    companion object {
        fun from(openReader: BodySource, length: Number?, charset: Charset = Charsets.UTF_8): Body {
            return Body(
                openReader = openReader,
                length = length,
                charset = charset
            )
        }
    }

}