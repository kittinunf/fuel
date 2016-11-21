package com.github.kittinunf.fuel.core

class HttpException(httpCode: Int, httpMessage: String) : Exception("HTTP Exception $httpCode $httpMessage")