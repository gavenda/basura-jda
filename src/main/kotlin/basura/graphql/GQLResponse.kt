package basura.graphql

import kotlinx.serialization.Serializable

@Serializable
data class GQLResponse<T>(
    val data: T
)
