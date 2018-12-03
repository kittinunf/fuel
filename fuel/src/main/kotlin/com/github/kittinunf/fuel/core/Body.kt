package com.github.kittinunf.fuel.core

import java.io.InputStream
import java.io.OutputStream

typealias BodySource = (() -> InputStream)
typealias BodyLength = (() -> Long)

interface Body {
    /**
     * Returns the body as a [ByteArray].
     *
     * @note Because the body needs to be read into memory anyway, implementations may choose to make the [Body]
     *  readable once more after calling this method, with the original [InputStream] being closed (and release its
     *  resources). This also means that if an implementation choose to keep it around, `isConsumed` returns false.
     *
     * @return the entire body
     */
    fun toByteArray(): ByteArray

    /**
     * Returns the body as an [InputStream].
     *
     * @note callers are responsible for closing the returned stream.
     * @note implementations may choose to make the [Body] `isConsumed` and can not be written or read from again.
     *
     * @return the body as input stream
     */
    fun toStream(): InputStream

    /**
     * Writes the body to the [OutputStream].
     *
     * @note callers are responses for closing the [OutputStream].
     * @note implementations may choose to make the [Body] `isConsumed` and can not be written or read from again.
     * @note implementations are recommended to buffer the output stream if they can't ensure bulk writing.
     *
     * @param outputStream [OutputStream] the stream to write to
     * @return [Long] the number of bytes written
     */
    fun writeTo(outputStream: OutputStream): Long

    /**
     * Returns the body emptiness.
     * @return [Boolean] if true, this body is empty
     */
    fun isEmpty(): Boolean

    /**
     * Returns if the body is consumed.
     * @return [Boolean] if true, `writeTo`, `toStream` and `toByteArray` may throw
     */
    fun isConsumed(): Boolean

    /**
     * Returns the length of the body in bytes
     * @return [Long?] the length in bytes, null if it is unknown
     */
    val length: Long?

    /**
     * Represents this body as a string
     * @param contentType [String] the type of the content in the body, or null if a guess is necessary
     * @return [String] the body as a string or a string that represents the body such as (empty) or (consumed)
     */
    fun asString(contentType: String?): String
}
