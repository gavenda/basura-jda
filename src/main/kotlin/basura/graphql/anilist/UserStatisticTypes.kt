package basura.graphql.anilist

import kotlinx.serialization.Serializable

@Serializable
data class UserStatisticTypes(
    val anime: UserStatistics,
    val manga: UserStatistics
)
