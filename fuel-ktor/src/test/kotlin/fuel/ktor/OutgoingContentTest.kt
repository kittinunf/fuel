package fuel.ktor

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.utils.EmptyContent
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

class OutgoingContentTest {
    @Test
    fun postWithByteArray() = runBlocking {
        val client = HttpClient()
        val post = client.post<ByteArray>("http://httpbin.org/post") {
            body = "Hello World".toByteArray()
        }
        assertNotNull(post)
    }

    @Test
    fun postWithEmptyContent() = runBlocking {
        val client = HttpClient()
        val response = client.post<String>("http://httpbin.org/post") {
            body = EmptyContent
        }
        assertNotNull(response)
    }

    @Test
    fun postWithWriteContent() = runBlocking {
        val client = HttpClient()
        val content = "Hello There!"
        val response = client.post<String>("http://httpbin.org/post") {
            body = object : OutgoingContent.WriteChannelContent() {
                override suspend fun writeTo(channel: ByteWriteChannel) {
                    channel.writeStringUtf8(content)
                    channel.close()
                }
            }
        }
        assertNotNull(response)
    }
}
