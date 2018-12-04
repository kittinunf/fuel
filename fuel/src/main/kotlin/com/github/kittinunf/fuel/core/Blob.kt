package com.github.kittinunf.fuel.core

import java.io.InputStream

@Deprecated("Use BlobDataPart (optionally wrapped in LazyDataPart) instead", ReplaceWith("BlobDataPart"))
data class Blob(
    val name: String = "",
    val length: Long,
    val inputStream: () -> InputStream
)