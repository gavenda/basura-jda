package basura.graphql.anilist

import kotlinx.serialization.Serializable

@Serializable
data class UserFormatStatistic(
    val count: Int = 0,
    val format: MediaFormat = MediaFormat.UNKNOWN
)
