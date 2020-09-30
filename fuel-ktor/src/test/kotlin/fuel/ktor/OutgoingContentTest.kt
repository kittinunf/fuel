package fuel.ktor

import io.ktor.client.HttpClient
import io.ktor.client.call.UnsupportedContentTypeException
import io.ktor.client.request.post
import io.ktor.client.utils.EmptyContent
import io.ktor.http.content.OutgoingContent
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.coroutines.CoroutineContext

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

    @KtorExperimentalAPI
    @Test(expected = UnsupportedContentTypeException::class)
    fun `should throw Error over protocol upgrade`() = runBlocking {
        val client = HttpClient()
        val response = client.post<String>("http://httpbin.org/post") {
            body = object : OutgoingContent.ProtocolUpgrade() {
                override suspend fun upgrade(
                    input: ByteReadChannel,
                    output: ByteWriteChannel,
                    engineContext: CoroutineContext,
                    userContext: CoroutineContext
                ): Job {
                    throw UnsupportedContentTypeException(this)
                }
            }
        }
        assertNotNull(response)
    }
}
