package basura.graphql.anilist

import kotlinx.serialization.Serializable

@Serializable
data class FuzzyDate(
    val year: Int = 0,
    val month: Int = 0,
    val day: Int = 0
)
