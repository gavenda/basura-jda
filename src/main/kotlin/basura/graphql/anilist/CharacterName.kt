package basura.graphql.anilist

import kotlinx.serialization.Serializable

/**
 * The names of the character
 */
@Serializable
data class CharacterName(
    /**
     * The character's first and last name
     */
    val full: String?,
    /**
     * The character's full name in their native language
     */
    val native: String?,
    /**
     * Other names the character might be referred to as
     */
    val alternative: List<String?>?
)
