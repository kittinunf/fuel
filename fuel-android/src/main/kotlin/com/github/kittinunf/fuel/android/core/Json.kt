package com.github.kittinunf.fuel.android.core

import org.json.JSONArray
import org.json.JSONObject

class Json(val content: String) {

    fun obj(): JSONObject = JSONObject(content)

    fun array(): JSONArray = JSONArray(content)

}