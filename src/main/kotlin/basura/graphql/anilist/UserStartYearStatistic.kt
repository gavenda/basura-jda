package basura.graphql.anilist

import kotlinx.serialization.Serializable

@Serializable
data class UserStartYearStatistic(
    val count: Int = 0,
    val startYear: Int = 0
)
