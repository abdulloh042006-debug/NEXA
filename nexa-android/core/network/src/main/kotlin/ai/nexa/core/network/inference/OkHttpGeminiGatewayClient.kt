package ai.nexa.core.network.inference

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

internal class OkHttpGeminiGatewayClient(
    baseUrl: String,
    private val sessionTokenProvider: () -> String?,
    private val client: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) : GeminiGatewayClient {
    private val endpoint = baseUrl.toHttpUrl().newBuilder()
        .addPathSegments("v1/inference/gemini:stream")
        .build()

    override fun stream(
        request: GeminiGatewayRequest,
        timeoutMillis: Long,
    ): Flow<GeminiGatewayEvent> = flow {
        val body = json.encodeToString(request.toNetworkRequest())
            .toRequestBody(JSON_MEDIA_TYPE)
        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(body)
            .header("Accept", NDJSON_MEDIA_TYPE)
            .header("Cache-Control", "no-store")
        sessionTokenProvider()?.takeIf(String::isNotBlank)?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }
        val call = client.newBuilder()
            .build().newCall(requestBuilder.build())
        call.timeout().timeout(timeoutMillis, TimeUnit.MILLISECONDS)
        val cancellationHandle = currentCoroutineContext()[Job]?.invokeOnCompletion { cause ->
            if (cause is kotlinx.coroutines.CancellationException) call.cancel()
        }

        try {
            call.execute().use { response ->
                response.requireSuccessful()
                val source = response.body?.source()
                    ?: throw InferenceGatewayException.ProtocolFailure(IOException("Missing response body"))
                while (currentCoroutineContext().isActive && !source.exhausted()) {
                    val line = source.readUtf8Line()?.takeIf(String::isNotBlank) ?: continue
                    emit(json.decodeFromString<NetworkEvent>(line).toGatewayEvent())
                }
            }
        } catch (error: kotlinx.coroutines.CancellationException) {
            call.cancel()
            throw error
        } catch (error: InferenceGatewayException) {
            throw error
        } catch (error: IOException) {
            currentCoroutineContext().ensureActive()
            throw InferenceGatewayException.ProtocolFailure(error)
        } catch (error: SerializationException) {
            throw InferenceGatewayException.ProtocolFailure(error)
        } catch (error: IllegalArgumentException) {
            throw InferenceGatewayException.ProtocolFailure(error)
        } finally {
            cancellationHandle?.dispose()
        }
    }.flowOn(Dispatchers.IO)

    private fun GeminiGatewayRequest.toNetworkRequest() = NetworkRequest(
        messages = messages.map { NetworkMessage(it.role, it.content) },
        generationConfig = GenerationConfig(
            temperature = temperature,
            topP = topP,
            maxOutputTokens = maxOutputTokens,
            stopSequences = stopSequences,
        ),
        language = language,
        privacyClass = privacyClass,
    )

    private fun NetworkEvent.toGatewayEvent(): GeminiGatewayEvent = when (type) {
        "token" -> GeminiGatewayEvent.Token(requireNotNull(text) { "token event is missing text" })
        "usage" -> GeminiGatewayEvent.Usage(
            inputTokens = requireNotNull(inputTokens) { "usage event is missing input_tokens" },
            outputTokens = requireNotNull(outputTokens) { "usage event is missing output_tokens" },
        )
        else -> throw IllegalArgumentException("Unknown inference event type: $type")
    }

    private fun Response.requireSuccessful() {
        val failure = when {
            code == HTTP_UNAUTHORIZED || code == HTTP_FORBIDDEN -> InferenceGatewayException.Unauthorized()
            code == HTTP_TOO_MANY_REQUESTS -> InferenceGatewayException.RateLimited()
            !isSuccessful -> InferenceGatewayException.HttpFailure(code)
            else -> null
        }
        failure?.let { throw it }
    }

    @Serializable
    private data class NetworkRequest(
        val messages: List<NetworkMessage>,
        @SerialName("generation_config") val generationConfig: GenerationConfig,
        val language: String?,
        @SerialName("privacy_class") val privacyClass: String,
    )

    @Serializable
    private data class NetworkMessage(val role: String, val content: String)

    @Serializable
    private data class GenerationConfig(
        val temperature: Double?,
        @SerialName("top_p") val topP: Double?,
        @SerialName("max_output_tokens") val maxOutputTokens: Int?,
        @SerialName("stop_sequences") val stopSequences: List<String>,
    )

    @Serializable
    private data class NetworkEvent(
        val type: String,
        val text: String? = null,
        @SerialName("input_tokens") val inputTokens: Int? = null,
        @SerialName("output_tokens") val outputTokens: Int? = null,
    )

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val NDJSON_MEDIA_TYPE = "application/x-ndjson"
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_TOO_MANY_REQUESTS = 429
    }
}

fun createGeminiGatewayClient(
    baseUrl: String,
    sessionTokenProvider: () -> String?,
): GeminiGatewayClient = OkHttpGeminiGatewayClient(baseUrl, sessionTokenProvider)
