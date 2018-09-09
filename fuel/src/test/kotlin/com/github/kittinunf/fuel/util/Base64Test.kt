package com.github.kittinunf.fuel.util

import org.junit.Test
import org.junit.Assert

class Base64Test {
    @Test
    fun testEncodedBase64ToString() {
        val encoded = "Hello World".encodeBase64ToString()
        Assert.assertEquals("SGVsbG8gV29ybGQ=", encoded)
    }

    @Test
    fun testDecodedStringToBase64() {
        Assert.assertEquals("SGVsbG8gV29ybGQ=".decodeBase64(), "Hello World")
    }

    @Test
    fun testEncodedBase64ToByteArray() {
        val encoded = "Hello World".encodeBase64ToByteArray()
        val byteArray = "SGVsbG8gV29ybGQ=".toByteArray()
        Assert.assertArrayEquals(byteArray, encoded)
    }

    @Test
    fun testDecodedBase64ToByteArray() {
        val decoded = "SGVsbG8gV29ybGQ=".decodeBase64ToByteArray()
        val byteArray = "Hello World".toByteArray()
        Assert.assertArrayEquals(decoded, byteArray)
    }

    @Test
    fun testEncodedByteArrayToString() {
        val byteArray = "Hello World".toByteArray().encodeBase64ToString()
        Assert.assertEquals(byteArray, "SGVsbG8gV29ybGQ=")
    }

    @Test
    fun testDecodedByteArrayToString() {
        val byteArray = "SGVsbG8gV29ybGQ=".toByteArray().decodeBase64ToString()
        Assert.assertEquals(byteArray, "Hello World")
    }
}
