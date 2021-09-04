package basura.graphql.anilist

import kotlinx.serialization.Serializable

@Serializable
data class MediaConnection(
    val nodes: List<Media?>? = null,
    val edges: List<MediaEdge?>? = null,
    val pageInfo: PageInfo? = null
)
