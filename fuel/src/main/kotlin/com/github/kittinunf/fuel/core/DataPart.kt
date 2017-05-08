package com.github.kittinunf.fuel.core

import java.io.File

data class DataPart(
        val fileName: String = "",
        val mediaType: String = "",
        val file: File)