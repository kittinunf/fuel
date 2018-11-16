package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.requests.DownloadRequest
import com.github.kittinunf.fuel.core.requests.UploadRequest

object Fuel : RequestFactory.Convenience by FuelManager.instance {
    var trace = false

    fun trace(function: () -> String) {
        @Suppress("ConstantConditionIf")
        if (trace) println(function())
    }

    fun reset() = FuelManager.instance.reset()
}

fun String.httpGet(parameters: Parameters? = null): Request =
    Fuel.get(this, parameters)

fun RequestFactory.PathStringConvertible.httpGet(parameter: Parameters? = null): Request =
    this.path.httpGet(parameter)

fun String.httpPost(parameters: Parameters? = null): Request =
    Fuel.post(this, parameters)

fun RequestFactory.PathStringConvertible.httpPost(parameters: Parameters? = null): Request =
    this.path.httpPost(parameters)

fun String.httpPut(parameters: Parameters? = null): Request =
    Fuel.put(this, parameters)

fun RequestFactory.PathStringConvertible.httpPut(parameter: Parameters? = null): Request =
    this.path.httpPut(parameter)

fun String.httpPatch(parameters: Parameters? = null): Request =
    Fuel.patch(this, parameters)

fun RequestFactory.PathStringConvertible.httpPatch(parameter: Parameters? = null): Request =
    this.path.httpPatch(parameter)

fun String.httpDelete(parameters: Parameters? = null): Request =
    Fuel.delete(this, parameters)

fun RequestFactory.PathStringConvertible.httpDelete(parameter: Parameters? = null): Request =
    this.path.httpDelete(parameter)

fun String.httpDownload(parameter: Parameters? = null, method: Method = Method.GET): DownloadRequest =
    Fuel.download(this, method, parameter)

fun RequestFactory.PathStringConvertible.httpDownload(parameters: Parameters? = null, method: Method = Method.GET): DownloadRequest =
    this.path.httpDownload(parameters, method)

fun String.httpUpload(parameters: Parameters? = null, method: Method = Method.POST): UploadRequest =
    Fuel.upload(this, method, parameters)

fun RequestFactory.PathStringConvertible.httpUpload(parameters: Parameters? = null, method: Method = Method.POST): UploadRequest =
    this.path.httpUpload(parameters, method)

fun String.httpHead(parameters: Parameters? = null): Request =
    Fuel.head(this, parameters)

fun RequestFactory.PathStringConvertible.httpHead(parameters: Parameters? = null): Request =
    this.path.httpHead(parameters)
