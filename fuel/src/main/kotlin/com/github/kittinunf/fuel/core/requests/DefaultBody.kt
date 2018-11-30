package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Body
import com.github.kittinunf.fuel.core.BodyLength
import com.github.kittinunf.fuel.core.BodySource
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.representationOfBytes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URLConnection
import java.nio.charset.Charset

data class DefaultBody(
    internal var openStream: BodySource = EMPTY_STREAM,
    private var calculateLength: BodyLength? = null,
    val charset: Charset = Charsets.UTF_8
) : Body {

    /**
     * Represents this body as a string
     * @param contentType [String] the type of the content in the body, or null if a guess is necessary
     * @return [String] the body as a string or a string that represents the body such as (empty) or (consumed)
     */
    override fun asString(contentType: String?): String {
        return when {
            isEmpty() -> "(empty)"
            isConsumed() -> "(consumed)"
            else -> representationOfBytes(contentType ?: URLConnection.guessContentTypeFromStream(openStream()))
        }
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

    /**
     * Returns the body as an [InputStream].
     *
     * @note callers are responsible for closing the returned stream.
     * @note implementations may choose to make the [Body] `isConsumed` and can not be written or read from again.
     *
     * @return the body as input stream
     */
    override fun toStream(): InputStream = openStream().buffered().apply {
        // The caller is now responsible for this stream. This make sure that you can't call this twice without handling
        // it. The caller must still call `.close()` on the returned value when done.
        openStream = CONSUMED_STREAM
    }

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

    /**
     * Returns the body emptiness.
     * @return [Boolean] if true, this body is empty
     */
    override fun isEmpty() = openStream === EMPTY_STREAM || (length == 0L)

    /**
     * Returns if the body is consumed.
     * @return [Boolean] if true, `writeTo`, `toStream` and `toByteArray` may throw
     */
    override fun isConsumed() = openStream === CONSUMED_STREAM

    /**
     * Returns the length of the body in bytes
     * @return [Long?] the length in bytes, null if it is unknown
     */
    override val length: Long? by lazy {
        calculateLength?.invoke()?.let {
            if (it == -1L) { null } else { it }
        }
    }

    /**
     * Makes the body repeatable by e.g. loading its contents into memory
     * @return [RepeatableBody] the body to be repeated
     */
    fun asRepeatable(): RepeatableBody = RepeatableBody(this)

    companion object {
        private val EMPTY_STREAM = {
            ByteArrayInputStream(ByteArray(0))
        }

        private val CONSUMED_STREAM = {
            throw FuelError.wrap(IllegalStateException(
                "The input has already been written to an output stream and can not be consumed again."
            ))
        }

        fun from(openStream: BodySource, calculateLength: BodyLength?, charset: Charset = Charsets.UTF_8): DefaultBody {
            return DefaultBody(
                openStream = openStream,
                calculateLength = calculateLength,
                charset = charset
            )
        }
    }
}