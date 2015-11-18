package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.Method
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 9/24/15.
 */

public class EncodingTest : BaseTestCase() {

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    @Test
    fun testEncodingNormal() {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = "http://www.example.com"
            urlString = "test"
            parameters = listOf("a" to "b")
        }.request

        assertTrue { "http://www.example.com/test?a=b" == request.url.toString() }
    }

    @Test
    fun testEncodingWithNoParam() {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = "http://www.example.com"
            urlString = "test"
            parameters = null
        }.request

        assertTrue { "http://www.example.com/test" == request.url.toString() }
    }

    @Test
    fun testEncodingWithIntegerParameter() {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = "http://www.example.com"
            urlString = "test"
            parameters = listOf("foo" to 1)
        }.request

        assertTrue { "http://www.example.com/test?foo=1" == request.url.toString() }
    }

    @Test
    fun testEncodingWithDoubleParameter() {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = "http://www.example.com"
            urlString = "test"
            parameters = listOf("foo" to 1.1)
        }.request

        assertTrue { "http://www.example.com/test?foo=1.1" == request.url.toString() }
    }

    @Test
    fun testEncodingWithBooleanParameter() {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = "http://www.example.com"
            urlString = "test"
            parameters = listOf("foo" to true)
        }.request

        assertTrue { "http://www.example.com/test?foo=true" == request.url.toString() }
    }

    @Test
    fun testEncodingWithNoParamAndNoRelativeUrlString() {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = "http://www.example.com/test"
            urlString = ""
            parameters = null
        }.request

        assertTrue { "http://www.example.com/test" == request.url.toString() }
    }

    @Test
    fun testEncodingWithJustBaseUrlString() {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = "http://www.example.com/test?a=b"
            urlString = ""
            parameters = null
        }.request

        assertTrue { "http://www.example.com/test?a=b" == request.url.toString() }
    }

    @Test
    fun testEncodingWithPercentEscapedCharacterPath() {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = "http://www.example.com/U$3|2|\\|@me/P@$\$vv0|2|)"
            urlString = ""
            parameters = null
        }.request

        assertTrue("http://www.example.com/U$3%7C2%7C%5C%7C@me/P@$\$vv0%7C2%7C)" == request.url.toString(), "Found ${request.url.toString()}")

    }

}