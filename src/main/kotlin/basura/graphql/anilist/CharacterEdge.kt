package basura.graphql.anilist

import kotlinx.serialization.Serializable

/**
 * Character connection edge
 */
@Serializable
data class CharacterEdge(
    /**
     * The characters role in the media
     */
    val role: CharacterRole?
)
