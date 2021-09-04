package basura.graphql.anilist

import kotlinx.serialization.Serializable

/**
 * Media connection edge
 */
@Serializable
data class MediaEdge(
    /**
     * The characters role in the media
     */
    val characterRole: CharacterRole?
)
