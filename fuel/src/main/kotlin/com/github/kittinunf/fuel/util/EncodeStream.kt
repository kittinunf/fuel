package com.github.kittinunf.fuel.util

import java.io.OutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream

/**
 * Encodes the [OutputStream] using [encoding]
 *
 * @param encoding [String] the encoding to apply, one of ("gzip", "deflate" or "identity")
 * @param unsupported fallback callback that is called if encoding is not supported natively
 * @return [OutputStream] the wrapped [OutputStream] that is encoded when it's written to
 */
fun OutputStream.encode(
    encoding: String,
    unsupported: (OutputStream, String) -> OutputStream = { _, _ ->
        throw UnsupportedOperationException("Encoding $encoding is not supported. Expected one of gzip, deflate, identity.")
    }
) = when (encoding.trim()) {
    "gzip" -> GZIPOutputStream(this)
    "deflate", "inflate" -> DeflaterOutputStream(this)
    // HTTPClient handles chunked
    "chunked", "identity", "" -> this
    else -> unsupported(this, encoding)
}