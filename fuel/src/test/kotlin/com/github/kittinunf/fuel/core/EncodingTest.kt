package com.github.kittinunf.fuel.core

import java.net.MalformedURLException
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Enclosed::class)
internal class EncodingTest {

    @RunWith(Parameterized::class)
    internal class Valid(
        private val inputUrl: String,
        private val encodedUrl: String
    ) {

        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "validUrl")
            fun data() = listOf(
                arrayOf("https://github.com/kittinunf/fuel/", "https://github.com/kittinunf/fuel/"),
                arrayOf(
                    "https://xn----f38am99bqvcd5liy1cxsg.test",
                    "https://xn----f38am99bqvcd5liy1cxsg.test"
                ),
                arrayOf("https://test.xn--rhqv96g", "https://test.xn--rhqv96g"),
                arrayOf("https://test.شبك", "https://test.xn--ngbx0c"),
                arrayOf("https://普遍接受-测试.top", "https://xn----f38am99bqvcd5liy1cxsg.top"),
                arrayOf(
                    "https://मेल.डाटामेल.भारत",
                    "https://xn--r2bi6d.xn--c2bd4bq1db8d.xn--h2brj9c"
                ),
                arrayOf("http://fußball.de", "http://xn--fuball-cta.de"),
                arrayOf("http://fußball.de", "http://xn--fuball-cta.de"),
            )
        }

        @Test
        fun testRequestURLIDNAEncoding() {
            val encoding = Encoding(
                httpMethod = Method.GET,
                urlString = inputUrl,
            )
            assertThat(encoding.request.url.toString(), equalTo(encodedUrl))
        }
    }


    @RunWith(Parameterized::class)
    internal class Invalid(
        private val inputUrl: String
    ) {

        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "invalidUrl")
            fun data() = listOf(
                "https://in--valid",
                "https://.test.top",
                "https://\\u0557w.test"
            )
        }

        @Test(expected = MalformedURLException::class)
        fun testRequestInvalidURL() {
            Encoding(httpMethod = Method.GET, urlString = inputUrl).request
        }
    }
}
