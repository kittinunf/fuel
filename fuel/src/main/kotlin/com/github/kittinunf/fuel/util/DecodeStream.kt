package com.github.kittinunf.fuel.util

import java.io.InputStream
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

private typealias DecodeFallbackCallback = (InputStream, String) -> InputStream
private val UNSUPPORTED_DECODE_ENCODING: DecodeFallbackCallback = { _, encoding ->
    throw UnsupportedOperationException("Decoding $encoding is not supported. Expected one of gzip, deflate, identity.")
}

/**
 * Return an [InputStream] that decodes [encoding] when it's read.
 *
 * @param encoding [String] the encoding ("gzip", "deflate", "chunked" or "identity")
 * @param unsupported [DecodeFallbackCallback] fallback callback that is called if encoding is not supported natively
 * @return [InputStream] the wrapped [InputStream] that is decoded when it's being read
 */
fun InputStream.decode(encoding: String, unsupported: DecodeFallbackCallback = UNSUPPORTED_DECODE_ENCODING) =
    when (encoding.trim()) {
        "gzip" -> GZIPInputStream(this)
        "deflate" -> InflaterInputStream(this)
        // HTTPClient handles chunked, but does not remove the Transfer-Encoding Header
        "chunked", "identity", "" -> this
        else -> unsupported(this, encoding)
    }

/**
 * Return an [InputStream] that decodes all [encodings] when it's read. They should be ordered from inner to outer, in
 * the order they were applied. A stream that was first deflated and then gzipped should be ("deflate", "gzip").
 *
 * @see decode
 *
 * @param encodings [Iterable<String>] all the encodings that should be decoded
 * @param unsupported [DecodeFallbackCallback] fallback callback that is called if encoding is not supported natively
 * @return [InputStream] the wrapped [InputStream] that is decoded when it's being read
 */
fun InputStream.decode(encodings: Iterable<String>, unsupported: DecodeFallbackCallback = UNSUPPORTED_DECODE_ENCODING) =
    encodings.fold(this) { stream, encoding -> stream.decode(encoding, unsupported) }