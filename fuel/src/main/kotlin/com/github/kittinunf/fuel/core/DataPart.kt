package com.github.kittinunf.fuel.core

import java.io.File

data class DataPart(
        val file: File,
        val name: String = file.name.split(".").getOrElse(0) { "" },
        val type: String = "")