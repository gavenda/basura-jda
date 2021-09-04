package basura.graphql.anilist

import kotlinx.serialization.Serializable

/**
 * List of anime or manga
 */
@Serializable
data class MediaList(
    /**
     * The id of the media
     */
    val mediaId: Long = 0,
    /**
     * The score of the entry
     */
    val score: Float = 0f,
    val status: MediaListStatus = MediaListStatus.UNKNOWN,
    val progress: Int = 0,
    val media: Media? = null,
    val user: User? = null
)
