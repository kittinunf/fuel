package com.github.kittinunf.fuel.core

class HttpException(val httpCode: Int, val httpMessage: String) : Exception("HTTP Exception $httpCode $httpMessage")