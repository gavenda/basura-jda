package basura.graphql

import basura.bot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kodein.di.instance

@Suppress("BlockingMethodInNonBlockingContext")
suspend inline fun <reified V, reified R> gqlQuery(graphUrl: String, query: String, variables: V): R {
    val json by bot.instance<Json>()
    val client by bot.instance<OkHttpClient>()
    val gqlRequest = GQLRequest(query, variables)
    val requestBody = json.encodeToString(gqlRequest)
        .toRequestBody()
    val request = Request.Builder()
        .url(graphUrl)
        .addHeader("Content-Type", "application/json")
        .post(requestBody)
        .build()

    val response = client.newCall(request).await()
    val responseBody = withContext(Dispatchers.IO) {
        response.body!!.string()
    }
    val gqlResponse = json.decodeFromString<GQLResponse<R>>(responseBody)

    return gqlResponse.data
}
