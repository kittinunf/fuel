package com.github.kittinunf.fuel.core

/**
 * Created by Kittinun Vantasin on 8/17/15.
 */

class HttpException(val httpCode: Int, val httpMessage: String) : Exception("HTTP Exception $httpCode $httpMessage")