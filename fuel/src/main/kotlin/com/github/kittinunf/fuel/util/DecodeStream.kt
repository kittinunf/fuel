package com.github.kittinunf.fuel.util

import java.io.InputStream
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

private typealias DecodeFallbackCallback = (InputStream, String) -> InputStream
private val UNSUPPORTED_DECODE_ENCODING: DecodeFallbackCallback = { _, encoding ->
    throw UnsupportedOperationException("Decoding $encoding is not supported. Expected one of gzip, deflate, identity.")
}

fun InputStream.decode(encoding: String, unsupported: DecodeFallbackCallback = UNSUPPORTED_DECODE_ENCODING) =
    when (encoding.trim()) {
        "gzip" -> GZIPInputStream(this)
        "deflate" -> InflaterInputStream(this)
        // HTTPClient handles chunked, but does not remove the Transfer-Encoding Header
        "chunked", "identity", "" -> this
        else -> unsupported(this, encoding)
    }

fun InputStream.decode(encodings: Iterable<String>, unsupported: DecodeFallbackCallback = UNSUPPORTED_DECODE_ENCODING) =
    encodings.fold(this) { stream, encoding -> stream.decode(encoding, unsupported)}