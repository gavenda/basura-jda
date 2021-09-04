package basura.graphql.anilist

import kotlinx.serialization.Serializable

/**
 * List of anime or manga
 */
@Serializable
data class MediaListCollection(
    /**
     * The owner of the list
     */
    val user: User?,
    /**
     * Grouped media list entries
     */
    val lists: List<MediaListGroup?>?
)
