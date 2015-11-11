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

}