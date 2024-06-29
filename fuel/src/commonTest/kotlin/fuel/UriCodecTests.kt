/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2022 Eliezer Graber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fuel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UriCodecTests {
    @Test
    fun testDecode_emptyString_returnsEmptyString() {
        assertEquals(
            "",
            UriCodec.decode(
                "",
                convertPlus = false,
                throwOnFailure = true
            )
        )
    }

    @Test
    fun testDecode_wrongHexDigit_fails() {
        try {
            // %p in the end.
            UriCodec.decode(
                "ab%2f$%C4%82%25%e0%a1%80%p",
                convertPlus = false,
                throwOnFailure = true
            )
            fail("Expected URISyntaxException")
        }
        catch(expected: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test
    fun testDecode_secondHexDigitWrong_fails() {
        try {
            // %1p in the end.
            UriCodec.decode(
                "ab%2f$%c4%82%25%e0%a1%80%1p",
                convertPlus = false,
                throwOnFailure = true
            )
            fail("Expected URISyntaxException")
        }
        catch(expected: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test
    fun testDecode_endsWithPercent_fails() {
        try {
            // % in the end.
            UriCodec.decode(
                "ab%2f$%c4%82%25%e0%a1%80%",
                convertPlus = false,
                throwOnFailure = true
            )
            fail("Expected URISyntaxException")
        }
        catch(expected: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test
    fun testDecode_dontThrowException_appendsUnknownCharacter() {
        assertEquals(
            "ab/$\u0102%\u0840\ufffd",
            UriCodec.decode(
                "ab%2f$%c4%82%25%e0%a1%80%",
                convertPlus = false,
                throwOnFailure = false
            )
        )
    }

    @Test
    fun testDecode_convertPlus() {
        assertEquals(
            "ab/$\u0102% \u0840",
            UriCodec.decode(
                "ab%2f$%c4%82%25+%e0%a1%80",
                convertPlus = true,
                throwOnFailure = false
            )
        )
    }

    // Last character needs decoding (make sure we are flushing the buffer with chars to decode).
    @Test
    fun testDecode_lastCharacter() {
        assertEquals(
            "ab/$\u0102%\u0840",
            UriCodec.decode(
                "ab%2f$%c4%82%25%e0%a1%80",
                convertPlus = false,
                throwOnFailure = true
            )
        )
    }

    // Check that a second row of encoded characters is decoded properly (internal buffers are
    // reset properly).
    @Test
    fun testDecode_secondRowOfEncoded() {
        assertEquals(
            "ab/$\u0102%\u0840aa\u0840",
            UriCodec.decode(
                "ab%2f$%c4%82%25%e0%a1%80aa%e0%a1%80",
                convertPlus = false,
                throwOnFailure = true
            )
        )
    }
}
