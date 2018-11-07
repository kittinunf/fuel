package com.github.kittinunf.fuel.util

import java.io.OutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream

fun OutputStream.encode(
        encoding: String,
        unsupported: (OutputStream, String) -> OutputStream = { _, _ ->
            throw UnsupportedOperationException("Encoding $encoding is not supported. Expected one of gzip, deflate, identity.")
        }
) = when(encoding.trim()) {
    "gzip" -> GZIPOutputStream(this)
    "deflate", "inflate" -> DeflaterOutputStream(this)
    // HTTPClient handles chunked
    "chunked", "identity", "" -> this
    else -> unsupported(this, encoding)
}