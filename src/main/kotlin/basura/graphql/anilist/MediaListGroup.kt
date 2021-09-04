package basura.graphql.anilist

import kotlinx.serialization.Serializable

/**
 * List group of anime or manga entries
 */
@Serializable
data class MediaListGroup(
    /**
     * Media list entries
     */
    val entries: List<MediaList?>?
)
