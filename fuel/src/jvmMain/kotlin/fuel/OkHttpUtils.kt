package fuel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import okhttp3.Call
import okhttp3.Response
import okhttp3.coroutines.executeAsync

@OptIn(ExperimentalCoroutinesApi::class)
public suspend fun Call.performAsync(): HttpResponse =
    withContext(Dispatchers.IO) {
        executeAsync().use { response ->
            val sourceBuffer = Buffer()
            sourceBuffer.write(response.body.bytes())
            HttpResponse().apply {
                statusCode = response.code
                source = sourceBuffer
                headers = response.toHeaders()
            }
        }
    }

@OptIn(ExperimentalCoroutinesApi::class)
public fun Call.performAsyncWithSSE(): Flow<String> =
    callbackFlow {
        val response = executeAsync()
        val reader = response.body.byteStream().bufferedReader()

        try {
            reader.useLines { lines ->
                for (line in lines) {
                    if (line.startsWith("data:")) {
                        val event = line.removePrefix("data:").trim()
                        trySendBlocking(event) // Ensure event is sent
                    }
                }
            }
        } catch (e: Exception) {
            close(e) // Properly close on error
        } finally {
            response.close() // Explicitly close HTTP response
        }

        awaitClose {
            response.close() // Ensure proper cleanup when the flow is cancelled
        }
    }

public fun Response.toHeaders(): Map<String, String> =
    headers
        .names()
        .mapNotNull { name ->
            headers[name]?.let { value -> name to value }
        }.toMap()
