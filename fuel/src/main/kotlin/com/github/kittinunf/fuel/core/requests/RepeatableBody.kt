package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Body
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * A Repeatable Body wraps a body and on the first [writeTo] it keeps the bytes in memory so it can be written again.
 *
 * Delegation is not possible because the [body] is re-assigned, and the delegation would point to the initial
 *   assignment.
 */
data class RepeatableBody(
    var body: Body
) : Body {
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
    override fun writeTo(outputStream: OutputStream): Long {
        val repeatableBodyStream = ByteArrayInputStream(toByteArray())
        return body.writeTo(outputStream)
                .also { length -> body = DefaultBody.from({ repeatableBodyStream }, { length }) }
    }

    /**
     * Returns the body as a [ByteArray].
     *
     * @note Because the body needs to be read into memory anyway, implementations may choose to make the [Body]
     *  readable once more after calling this method, with the original [InputStream] being closed (and release its
     *  resources). This also means that if an implementation choose to keep it around, `isConsumed` returns false.
     *
     * @return the entire body
     */
    override fun toByteArray() = body.toByteArray()

    /**
     * Returns the body as an [InputStream].
     *
     * @note callers are responsible for closing the returned stream.
     * @note implementations may choose to make the [Body] `isConsumed` and can not be written or read from again.
     *
     * @return the body as input stream
     */
    override fun toStream() = body.toStream()

    /**
     * Returns the body emptiness.
     * @return [Boolean] if true, this body is empty
     */
    override fun isEmpty() = body.isEmpty()

    /**
     * Returns if the body is consumed.
     * @return [Boolean] if true, `writeTo`, `toStream` and `toByteArray` may throw
     */
    override fun isConsumed() = body.isConsumed()

    /**
     * Represents this body as a string
     * @param contentType [String] the type of the content in the body, or null if a guess is necessary
     * @return [String] the body as a string or a string that represents the body such as (empty) or (consumed)
     */
    override fun asString(contentType: String?) = body.asString(contentType)

    /**
     * Returns the length of the body in bytes
     * @return [Long?] the length in bytes, null if it is unknown
     */
    override val length = body.length
}