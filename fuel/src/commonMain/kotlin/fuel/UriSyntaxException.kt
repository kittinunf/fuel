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

/**
 * Exception thrown to indicate that a string could not be parsed as a URI reference.
 */
public class UriSyntaxException(
    /**
     * The input string.
     */
    public val input: String,
    /**
     * Returns a string explaining why the input string could not be parsed.
     */
    private val internalReason: String,
    /**
     * An index into the input string of the position at which the
     * parse error occurred, or `-1` if this position is not known.
     */
    public val index: Int = -1
) : Exception(internalReason) {
    init {
        require(index >= -1)
    }

    public val reason: String
        get() = message

    public override val message: String
        get() = buildString {
            append(internalReason)
            if(index > -1) {
                append(" at index ")
                append(index)
            }
            append(": ")
            append(input)
        }
}
