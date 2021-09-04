package basura.graphql.anilist

import kotlinx.serialization.Serializable

/**
 * The official titles of the media in various languages
 */
@Serializable
data class MediaTitle(
    /**
     * The official english title
     */
    val english: String?,
    /**
     * The romanization of the native language title
     */
    val romaji: String?,
    /**
     * Official title in it's native language
     */
    val native: String?
)
