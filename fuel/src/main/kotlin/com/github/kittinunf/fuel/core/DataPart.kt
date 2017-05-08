package com.github.kittinunf.fuel.core

import java.io.File

data class DataPart(
        var fileName: String = "",
        var mediaType: String = "",
        var file: File)