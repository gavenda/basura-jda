package basura

import basura.graphql.anilist.MediaListStatus

data class EmbedMedia(
    val discordName: String?,
    val status: MediaListStatus,
    val score: Float,
    val progress: Int
)
