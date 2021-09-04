package basura.graphql

import kotlinx.serialization.Serializable

@Serializable
data class GQLRequest<T>(
    val query: String,
    val variables: T
)