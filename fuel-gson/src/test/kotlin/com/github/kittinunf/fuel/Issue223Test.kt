package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.gson.responseObject
import org.hamcrest.CoreMatchers.isA
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Tests for https://github.com/kittinunf/Fuel/issues/233
 */
class Issue223Test {
    init {
        Fuel.testMode {
            timeout = 15000
        }
    }

    data class IssueInfo(val id: Int, val title: String, val number: Int)

    @Test
    fun testProcessingGenericList() {
        Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues").responseObject<List<IssueInfo>> { _, _, result ->
            val issues = result.get()
            assertNotEquals(issues.size, 0)
            assertThat(issues[0], isA(IssueInfo::class.java))
        }
    }
}