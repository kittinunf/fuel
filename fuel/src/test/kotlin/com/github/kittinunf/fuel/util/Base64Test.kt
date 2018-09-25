/**
 * Copied From https://github.com/square/okio/blob/master/okio/src/test/kotlin/okio/ByteStringTest.kt
 */

package com.github.kittinunf.fuel.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class Base64Test {
    @Test
    fun encodeBase64() {
        assertEquals("", "".encodeBase64ToString())
        assertEquals("AA==", "\u0000".encodeBase64ToString())
        assertEquals("AAA=", "\u0000\u0000".encodeBase64ToString())
        assertEquals("AAAA", "\u0000\u0000\u0000".encodeBase64ToString())
        assertEquals("SG93IG1hbnkgbGluZXMgb2YgY29kZSBhcmUgdGhlcmU/ICdib3V0IDIgbWlsbGlvbi4=",
                "How many lines of code are there? 'bout 2 million.".encodeBase64ToString())
    }

    @Test
    fun encodeBase64Url() {
        assertEquals("", "".encodeBase64UrlToString())
        assertEquals("AA==", "\u0000".encodeBase64UrlToString())
        assertEquals("AAA=", "\u0000\u0000".encodeBase64UrlToString())
        assertEquals("AAAA", "\u0000\u0000\u0000".encodeBase64UrlToString())
        assertEquals("SG93IG1hbnkgbGluZXMgb2YgY29kZSBhcmUgdGhlcmU_ICdib3V0IDIgbWlsbGlvbi4=",
                "How many lines of code are there? 'bout 2 million.".encodeBase64UrlToString())
    }

    @Test
    fun ignoreUnnecessaryPadding() {
        assertEquals(null, "\\fgfgff\\".decodeBase64ToString())
        assertEquals("", "====".decodeBase64ToString())
        assertEquals("\u0000\u0000\u0000", "AAAA====".decodeBase64ToString())
    }

    @Test
    fun decodeBase64() {
        assertArrayEquals("".toByteArray(), "".decodeBase64())
        assertEquals("", "".decodeBase64ToString())
        assertEquals(null, "/===".decodeBase64ToString()) // Can't do anything with 6 bits!
        assertEquals("What's to be scared about? It's just a little hiccup in the power...",
                ("V2hhdCdzIHRvIGJlIHNjYXJlZCBhYm91dD8gSXQncyBqdXN0IGEgbGl0dGxlIGhpY2" +
                        "N1cCBpbiB0aGUgcG93ZXIuLi4=").decodeBase64ToString())
        assertEquals("How many lines of code are there>", "SG93IG1hbnkgbGluZXMgb2YgY29kZSBhcmUgdGhlcmU+".decodeBase64ToString())
    }

    @Test
    fun decodeBase64WithWhitespace() {
        assertEquals("\u0000\u0000\u0000", " AA AA ".decodeBase64ToString())
        assertEquals("\u0000\u0000\u0000", " AA A\r\nA ".decodeBase64ToString())
        assertEquals("\u0000\u0000\u0000", "AA AA".decodeBase64ToString())
        assertEquals("\u0000\u0000\u0000", " AA AA ".decodeBase64ToString())
        assertEquals("\u0000\u0000\u0000", " AA A\r\nA ".decodeBase64ToString())
        assertEquals("\u0000\u0000\u0000", "A    AAA".decodeBase64ToString())
        assertEquals("", "    ".decodeBase64ToString())
    }
}