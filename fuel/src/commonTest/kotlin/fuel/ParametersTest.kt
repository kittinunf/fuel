package fuel

import kotlin.test.Test
import kotlin.test.assertEquals

class ParametersTest {
    @Test
    fun parametersWithQuestionMarkOnURL() {
        val test = "http://example.com?".fillURLWithParameters(listOf("test" to "url"))
        assertEquals("http://example.com?&test=url", test)
    }

    @Test
    fun parametersWithQuestionMarkOnParameter() {
        val test = "http://example.com".fillURLWithParameters(listOf("?test" to "url"))
        assertEquals("http://example.com?%3Ftest=url", test)
    }

    @Test
    fun parametersWithEmptyParameter() {
        val test = "http://example.com".fillURLWithParameters(listOf())
        assertEquals("http://example.com?", test)
    }

    @Test
    fun parametersWithQuestionMarkAndEmptyParameter() {
        val test = "http://example.com?".fillURLWithParameters(listOf())
        assertEquals("http://example.com?", test)
    }
}
