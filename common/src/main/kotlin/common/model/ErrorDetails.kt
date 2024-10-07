package common.model

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ErrorDetails(
    val status: Int,
    val message: String?,
) {
    companion object {
        val json =
            Json {
                ignoreUnknownKeys = true
            }

        fun response(
            status: HttpStatusCode,
            message: String?,
        ) = TextContent(json.encodeToString(ErrorDetails(status.value, message)), ContentType.Application.Json, status)
    }
}
