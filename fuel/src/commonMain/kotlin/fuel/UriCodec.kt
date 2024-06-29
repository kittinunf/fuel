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

public object UriCodec {
    /**
     * Encodes characters in the given string as '%'-escaped octets
     * using the UTF-8 scheme. Leaves letters ("A-Z", "a-z"), numbers
     * ("0-9"), and unreserved characters ("_-!.~'()*") intact. Encodes
     * all other characters.
     *
     * @param s string to encode
     * @return an encoded version of s suitable for use as a URI component,
     *  or null if s is null
     */
    public fun encodeOrNull(s: String?): String? = if (s == null) null else encode(s, null)

    /**
     * Encodes characters in the given string as '%'-escaped octets
     * using the UTF-8 scheme. Leaves letters ("A-Z", "a-z"), numbers
     * ("0-9"), and unreserved characters ("_-!.~'()*") intact. Encodes
     * all other characters with the exception of those specified in the
     * allow argument.
     *
     * @param s string to encode
     * @param allow set of additional characters to allow in the encoded form,
     *  null if no characters should be skipped
     * @return an encoded version of s suitable for use as a URI component,
     *  or null if s is null
     */
    public fun encodeOrNull(
        s: String?,
        allow: String?,
    ): String? = if (s == null) null else encode(s, allow)

    /**
     * Encodes characters in the given string as '%'-escaped octets
     * using the UTF-8 scheme. Leaves letters ("A-Z", "a-z"), numbers
     * ("0-9"), and unreserved characters ("_-!.~'()*") intact. Encodes
     * all other characters.
     *
     * @param s string to encode
     * @return an encoded version of s suitable for use as a URI component,
     *  or null if s is null
     */
    public fun encode(s: String): String = encode(s, null)

    /**
     * Encodes characters in the given string as '%'-escaped octets
     * using the UTF-8 scheme. Leaves letters ("A-Z", "a-z"), numbers
     * ("0-9"), and unreserved characters ("_-!.~'()*") intact. Encodes
     * all other characters with the exception of those specified in the
     * allow argument.
     *
     * @param s string to encode
     * @param allow set of additional characters to allow in the encoded form,
     *  null if no characters should be skipped
     * @return an encoded version of s suitable for use as a URI component
     */
    public fun encode(
        s: String,
        allow: String?,
    ): String {
        // Lazily-initialized buffers.
        var encoded: StringBuilder? = null

        val oldLength: Int = s.length

        // This loop alternates between copying over allowed characters and
        // encoding in chunks. This results in fewer method calls and
        // allocations than encoding one character at a time.
        var current = 0
        while (current < oldLength) {
            // Start in "copying" mode where we copy over allowed chars.

            // Find the next character which needs to be encoded.
            var nextToEncode = current
            while (nextToEncode < oldLength && isAllowed(s[nextToEncode], allow)) {
                nextToEncode++
            }

            // If there's nothing more to encode...
            if (nextToEncode == oldLength) {
                return if (current == 0) {
                    // We didn't need to encode anything!
                    s
                } else {
                    // Presumably, we've already done some encoding.
                    encoded!!.append(s, current, oldLength)
                    encoded.toString()
                }
            }

            if (encoded == null) {
                encoded = StringBuilder()
            }

            if (nextToEncode > current) {
                // Append allowed characters leading up to this point.
                encoded.append(s, current, nextToEncode)
            } else {
                // assert nextToEncode == current
            }

            // Switch to "encoding" mode.

            // Find the next allowed character.
            current = nextToEncode
            var nextAllowed = current + 1
            while (nextAllowed < oldLength && !isAllowed(s[nextAllowed], allow)) {
                nextAllowed++
            }

            // Convert the substring to bytes and encode the bytes as
            // '%'-escaped octets.
            val toEncode = s.substring(current, nextAllowed)
            try {
                val bytes: ByteArray = toEncode.encodeToByteArray()
                val bytesLength = bytes.size
                for (i in 0 until bytesLength) {
                    encoded.append('%')
                    encoded.append(hexDigits[bytes[i].toInt() and 0xf0 shr 4])
                    encoded.append(hexDigits[bytes[i].toInt() and 0xf])
                }
            } catch (e: Exception) {
                throw AssertionError(e)
            }
            current = nextAllowed
        }

        ByteArray(0).decodeToString()

        // Encoded could still be null at this point if s is empty.
        return encoded?.toString() ?: s
    }

    /**
     * Returns true if the given character is allowed.
     *
     * @param c character to check
     * @param allow characters to allow
     * @return true if the character is allowed or false if it should be
     * encoded
     */
    private fun isAllowed(
        c: Char,
        allow: String?,
    ): Boolean =
        c in lowercaseAsciiAlphaRange ||
            c in uppercaseAsciiAlphaRange ||
            c in digitAsciiRange ||
            c in defaultAllowedSet ||
            allow != null &&
            allow.indexOf(c) != -1

    /**
     * Decodes '%'-escaped octets in the given string using the UTF-8 scheme.
     * Replaces invalid octets with the unicode replacement character
     * ("\\uFFFD").
     *
     * @param s encoded string to decode
     * @param convertPlus if `convertPlus == true` all ‘+’ chars in the decoded output are converted to ‘ ‘
     *  (white space)
     * @param throwOnFailure if `throwOnFailure == true` an [IllegalArgumentException] is thrown for
     *  invalid inputs. Else, U+FFd is emitted to the output in place of invalid input octets.
     * @return the given string with escaped octets decoded, or null if s is null
     */
    public fun decodeOrNull(
        s: String?,
        convertPlus: Boolean = false,
        throwOnFailure: Boolean = false,
    ): String? = if (s == null) null else decode(s, convertPlus, throwOnFailure)

    /**
     * Decodes '%'-escaped octets in the given string using the UTF-8 scheme.
     * Replaces invalid octets with the unicode replacement character
     * ("\\uFFFD").
     *
     * @param s encoded string to decode
     * @param convertPlus if `convertPlus == true` all ‘+’ chars in the decoded output are converted to ‘ ‘
     *  (white space)
     * @param throwOnFailure if `throwOnFailure == true` an [IllegalArgumentException] is thrown for
     *  invalid inputs. Else, U+FFd is emitted to the output in place of invalid input octets.
     * @return the given string with escaped octets decoded
     */
    public fun decode(
        s: String,
        convertPlus: Boolean = false,
        throwOnFailure: Boolean = false,
    ): String {
        val builder = StringBuilder(s.length)

        // Holds the bytes corresponding to the escaped chars being read
        // (empty if the last char wasn't a escaped char).
        ByteBuffer(s.length).apply {
            var i = 0
            while (i < s.length) {
                when (val c = s[i++]) {
                    '+' -> {
                        flushDecodingByteAccumulator(builder, throwOnFailure)
                        builder.append(if (convertPlus) ' ' else '+')
                    }

                    '%' -> {
                        // Expect two characters representing a number in hex.
                        var hexValue: Byte = 0
                        for (@Suppress("UnusedPrivateProperty") j in 0..1) {
                            val nextC =
                                try {
                                    getNextCharacter(s, i, s.length, name = null)
                                } catch (e: UriSyntaxException) {
                                    // Unexpected end of input.
                                    if (throwOnFailure) {
                                        throw IllegalArgumentException(e)
                                    } else {
                                        flushDecodingByteAccumulator(builder, throwOnFailure)
                                        builder.append(INVALID_INPUT_CHARACTER)
                                        return builder.toString()
                                    }
                                }
                            i++
                            val newDigit: Int = hexCharToValue(nextC)
                            if (newDigit < 0) {
                                if (throwOnFailure) {
                                    throw IllegalArgumentException(
                                        unexpectedCharacterException(s, name = null, nextC, i - 1),
                                    )
                                } else {
                                    flushDecodingByteAccumulator(builder, throwOnFailure)
                                    builder.append(INVALID_INPUT_CHARACTER)
                                    break
                                }
                            }
                            hexValue = (hexValue * 0x10 + newDigit).toByte()
                        }
                        writeByte(hexValue)
                    }

                    else -> {
                        flushDecodingByteAccumulator(builder, throwOnFailure)
                        builder.append(c)
                    }
                }
            }

            flushDecodingByteAccumulator(builder, throwOnFailure)
        }

        return builder.toString()
    }

    private class ByteBuffer(private val size: Int) {
        private val buffer by lazy {
            ByteArray(size) { 0 }
        }

        var writePosition = 0
            private set

        fun writeByte(byte: Byte) {
            buffer[writePosition++] = byte
        }

        fun decodeToStringAndReset() =
            try {
                buffer.decodeToString(
                    startIndex = 0,
                    endIndex = writePosition,
                    throwOnInvalidSequence = false,
                )
            } finally {
                writePosition = 0
            }
    }

    private inline fun ByteBuffer.flushDecodingByteAccumulator(
        builder: StringBuilder,
        throwOnFailure: Boolean,
    ) {
        if (writePosition == 0) return

        try {
            builder.append(decodeToStringAndReset())
        } catch (e: Exception) {
            if (throwOnFailure) {
                throw IllegalArgumentException(e)
            } else {
                builder.append(INVALID_INPUT_CHARACTER)
            }
        }
    }

    private fun unexpectedCharacterException(
        uri: String,
        name: String?,
        unexpected: Char,
        index: Int,
    ): UriSyntaxException {
        val nameString = if (name == null) "" else " in [$name]"
        return UriSyntaxException(
            uri,
            "Unexpected character$nameString: $unexpected",
            index,
        )
    }

    private fun getNextCharacter(
        uri: String,
        index: Int,
        end: Int,
        name: String?,
    ): Char {
        if (index >= end) {
            val nameString = if (name == null) "" else " in [$name]"
            throw UriSyntaxException(uri, "Unexpected end of string $nameString", index)
        }
        return uri[index]
    }

    /**
     * Interprets a char as hex digits, returning a number from -1 (invalid char) to 15 ('f').
     */
    private fun hexCharToValue(c: Char): Int =
        when (c) {
            in digitAsciiRange -> c.code - '0'.code
            in lowercaseHexRange -> 10 + c.code - 'a'.code
            in uppercaseHexRange -> 10 + c.code - 'A'.code
            else -> -1
        }

    private val lowercaseAsciiAlphaRange = 'a'..'z'
    private val lowercaseHexRange = 'a'..'f'
    private val uppercaseAsciiAlphaRange = 'A'..'Z'
    private val uppercaseHexRange = 'A'..'F'
    private val digitAsciiRange = '0'..'9'
    private val defaultAllowedSet = setOf('_', '-', '!', '.', '~', '\'', '(', ')', '*')
    private val hexDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    /**
     * Character to be output when there's an error decoding an input.
     */
    private const val INVALID_INPUT_CHARACTER = '\ufffd'
}
